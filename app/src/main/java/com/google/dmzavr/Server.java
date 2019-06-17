package com.google.dmzavr;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentActivity;

import com.example.tmsemu.TankListViewModel;
import com.example.tmsemu.TankViewModel;
import com.example.tmsemu.TanksView;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

class TLSProto {
    private static byte[] dbl2HexFloat(Double in, boolean forceNull) {
        ByteBuffer bb = ByteBuffer.allocate(50);
        bb.putFloat((null == in || forceNull) ? Float.NEGATIVE_INFINITY : in.floatValue());
        bb.flip();
        return String.format("%08X", bb.getInt()).getBytes();
    }

    private static byte[] dbl2HexFloat(Double in, boolean forceNull, float multiplier) {
        ByteBuffer bb = ByteBuffer.allocate(50);
        bb.putFloat((null == in || forceNull) ? 0 : in.floatValue() * multiplier);
        bb.flip();
        return String.format("%08X", bb.getInt()).getBytes();
    }

    private static byte[] addChkSumAndETX(ByteBuffer bb)
    {
        int chksum = 0;
        for( int i = 0; i < bb.position(); ++i) {
            chksum += bb.get(i);
            chksum = chksum & 0xffff;
        }
        int chksum_ = 0xffff - chksum + 1;
        bb.put( String.format("%04X", chksum_).getBytes() )
                .put( (byte)0x3 );

        return Arrays.copyOf( bb.array(), bb.position() );
    }

    private interface ITLSChannelReplyChunk {
        int get_out_chunk_size();
        void make_reply(ByteBuffer byteBuffer, int channel, TankViewModel.TankData td);
    }

    private static class I214I234Reply implements ITLSChannelReplyChunk {
        @Override
        public int get_out_chunk_size() {
            return 100;
        }

        @Override
        public void make_reply(ByteBuffer bb, int channel, TankViewModel.TankData td) {
//            <SOH>i214TTYYMMDDHHmmTTpssssNNFFFFFFFF...
//                                 TTpssssNNFFFFFFFF&&CCCC<ETX>

//            1. YYMMDDHHmm - Current Date and Time
//            2. TT - Tank Number (Decimal, 00=all)
//            3. p - Product Code (single ASCII character [20h-7Eh])
//            4. ssss - Tank Status Bits:
//                  Bit 1=(LSB) Delivery in Progress
//                  Bit 2=Leak Test in Progress
//                  Bit 3=Invalid Fuel Height Alarm (MAG Probes Only)
//                  Bit 4-16 - Unused
//            5. NN - Number of eight character Data Fields to follow (Hex)
//            6. FFFFFFFF - ASCII Hex IEEE float:
//                  1. Volume
//                  2. Mass
//                  3. Density
//                  4. Height
//                  5. Water
//                  6. Temperature
//                  7. TC Density
//                  8. TC Volume
//                  9. Ullage
//                  10. Water Volume
//                  11. Total TC Density Offset
//            7. && - Data Termination Flag
//            8. CCCC - Message Checksum

            TankViewModel.TankSample sample = null == td || null == td.sample ? new TankViewModel.TankSample() : td.sample;
            TankViewModel.TankCfg cfg = null == td || null == td.cfg ? new TankViewModel.TankCfg() : td.cfg;

            double ullage = null == td || null == td.cfg.volume || null == sample.fuelVolume || null == sample.waterVolume
                    ? 0.
                    : td.cfg.volume - sample.fuelVolume - sample.waterVolume;
            Double tls_volume = (null == sample.fuelVolume && null == sample.waterVolume)
                    || (!cfg.hasFuelVolume && !cfg.hasWaterVolume)
                    ? null
                    : (
                    (null == sample.fuelVolume ? 0. : sample.fuelVolume)
                            + (null == sample.waterVolume ? 0. : sample.waterVolume)
            );

            bb.put(String.format(Locale.US, "%02d", channel).getBytes())
                    .put((byte) (48 + (channel % 80)))
                    .put("0000".getBytes())
                    .put("0B".getBytes())

                    .put(dbl2HexFloat(tls_volume,false))
                    .put(dbl2HexFloat(sample.fuelMass, !cfg.hasMass))
                    .put(dbl2HexFloat(sample.fuelDensity, !cfg.hasDensity, 1000f))
                    .put(dbl2HexFloat(sample.fuelLevel, !cfg.hasTotalLevel))
                    .put(dbl2HexFloat(sample.waterLevel, !cfg.hasWaterLevel))
                    .put(dbl2HexFloat(sample.temperature, !cfg.hasTemperature))
                    .put(dbl2HexFloat(sample.tcFuelDensity, !cfg.hasTcDensity, 1000f))
                    .put(dbl2HexFloat(sample.tcFuelVolume, !cfg.hasTcVolume))
                    .put(dbl2HexFloat(ullage, !cfg.hasUllage))
                    .put(dbl2HexFloat(sample.waterVolume, !cfg.hasWaterVolume))
                    .put(dbl2HexFloat(0.,false))
            ;
        }
    }

    private static class I205Reply implements ITLSChannelReplyChunk {
        @Override
        public int get_out_chunk_size() {
            return 20;
        }

        @Override
        public void make_reply(ByteBuffer bb, int channel, TankViewModel.TankData td) {
//            Typical Response Message, Computer Format:
//                  <SOH>i205TTYYMMDDHHmmTTnnAA...
//                           TTnnAA...&&CCCC<ETX>
//            Notes:
//            1. YYMMDDHHmm - Current Date and Time
//            2. TT - Tank Number (Decimal, 00 = all)
//            3. nn - Number of alarms active for tank (Hex, 00 = none)
//            4. AA - Active tank alarm type:
//               03 = Tank High Water Alarm
//               04 = Tank Overfill Alarm
//               05 = Tank Low Product Alarm
//               08 = Tank Invalid Fuel Level Alarm
//               09 = Tank Probe Out Alarm
//               11 = Tank Delivery Needed Warning
//               12 = Tank Maximum Product Alarm
//               13 = Tank Gross Leak Test Fail Alarm
//               14 = Tank Periodic Leak Test Fail Alarm
//               15 = Tank Annual Leak Test Fail Alarm
//               27 = Tank Cold Temperature Warning
//            5. && - Data Termination Flag
//            6. CCCC - Message Checksum
            bb.put( String.format( Locale.US, "%02d", channel).getBytes() ).put("00".getBytes());
        }
    }

    private static class I201Reply implements ITLSChannelReplyChunk {
        @Override
        public int get_out_chunk_size() {
            return 100;
        }

        @Override
        public void make_reply(ByteBuffer bb, int channel, TankViewModel.TankData td) {
//            Typical Response Message, Computer Format:
//                  <SOH>i201TTYYMMDDHHmmTTpssssNNFFFFFFFF...
//                           TTpssssNNFFFFFFFF...&&CCCC<ETX>
//            Notes:
//            1. YYMMDDHHmm - Current Date and Time
//            2.         TT - Tank Number (Decimal, 00 = all)
//            3.          p - Product Code (single ASCII character, from 20 Hex - 7E Hex)
//            4.       ssss - Tank Status Bits:
//                              Bit 1 - (LSB) Delivery in Progress
//                              Bit 2 - Leak Test in Progress
//                              Bit 3 - Invalid Fuel Height Alarm (MAG Probes Only)
//                              Bit 4-16 - Unused
//            5.         NN - Number of eight character Data Fields to follow (Hex)
//            6.   FFFFFFFF - ASCII Hex IEEE float:
//                              1. Volume
//                              2. TC Volume
//                              3. Ullage
//                              4. Height
//                              5. Water
//                              6. Temperature
//                              7. Water Volume
//            7.         && - Data Termination Flag
//            8.       CCCC - Message Checksum

            TankViewModel.TankSample sample = null == td || null == td.sample ? new TankViewModel.TankSample() : td.sample;
            TankViewModel.TankCfg cfg = null == td || null == td.cfg ? new TankViewModel.TankCfg() : td.cfg;

            double ullage = null == td || null == td.cfg.volume || null == sample.fuelVolume || null == sample.waterVolume
                    ? 0.
                    : td.cfg.volume - sample.fuelVolume - sample.waterVolume
                    ;
            Double tls_volume = (null == sample.fuelVolume && null == sample.waterVolume)
                    || (!cfg.hasFuelVolume && !cfg.hasWaterVolume)
                    ? null
                    : (
                    (null == sample.fuelVolume ? 0. : sample.fuelVolume)
                            + (null == sample.waterVolume ? 0. : sample.waterVolume)
            );

            bb.put( String.format( Locale.US, "%02d", channel).getBytes() )
                    .put((byte)(48 + (channel % 80)))
                    .put("0000".getBytes())
                    .put("07".getBytes())

                    .put( dbl2HexFloat( tls_volume, false ) )
                    .put( dbl2HexFloat( sample.tcFuelVolume, !cfg.hasTcVolume ) )
                    .put( dbl2HexFloat( ullage, !cfg.hasUllage ) )
                    .put( dbl2HexFloat( sample.fuelLevel, !cfg.hasTotalLevel ) )
                    .put( dbl2HexFloat( sample.waterLevel, !cfg.hasWaterLevel ) )
                    .put( dbl2HexFloat( sample.temperature, !cfg.hasTemperature ) )
                    .put( dbl2HexFloat( sample.waterVolume, !cfg.hasWaterVolume ) )
            ;

        }
    }
    private static byte[] makeByChannelsReply(FragmentActivity activity, String cmd, ITLSChannelReplyChunk replyChunkGenerator )
    {
        int req_channel = Integer.parseInt( cmd.substring(4, 6) );

        TankListViewModel model = ViewModelProviders.of(activity, TmsEmuStorage.getInstance().getFactory(-1) ).get(TankListViewModel.class);
        List<Integer> channels = model.getTankList().getValue();

        ByteBuffer bb = ByteBuffer.allocate( 30 + replyChunkGenerator.get_out_chunk_size() * ( req_channel > 0 ? 1 : ( null == channels ? 0 : channels.size() ) ) );

        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMddHHmm", Locale.US );

        bb.put ( (byte)1 )
                .put( cmd.getBytes() )
                .put( fmt.format(date).getBytes() )
        ;

        if( null != channels ) {
            for (int ch : channels) {
                if (0 < req_channel && req_channel != ch)
                    continue;

                TankViewModel datamodel = ViewModelProviders.of(activity, TmsEmuStorage.getInstance().getFactory(ch)).get(TankViewModel.viewModelKey(ch), TankViewModel.class);
                TankViewModel.TankData td = datamodel.getTankData().getValue();
                replyChunkGenerator.make_reply(bb, ch, td);
            }
        }
        return addChkSumAndETX(bb.put("&&".getBytes()));
    }

    private static byte[] make_i902_reply() {
//            Typical Response Message, Computer Format:
//                <SOH>i90200YYMMDDHHmmSOFTWARE# nnnnnn-vvv-rrrCREATED - YY.MM.DD.HH.mm&&CCCC<ETX>
//                    Notes:
//            1.     YYMMDDHHmm - Current Date and Time
//            2.     nnnnnn-vvv - Software version number (ASCII text string)
//            3.            rrr - Software revision level (ASCII text string)
//            4. YY.MM.DD.HH.mm - Date and time of software creation
//            5.             && - Data Termination Flag
//            6.           CCCC - Message Checksum

        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMddHHmm", Locale.US );

        ByteBuffer bb = ByteBuffer.allocate( 100 );
        bb.put ( (byte)1 )
                .put( "i90200".getBytes() )
                .put( fmt.format(date).getBytes() )
                .put( "SOFTWARE# dmzavr-001-001CREATED - 19.06.16.18.00".getBytes())
        ;
        return addChkSumAndETX(bb.put("&&".getBytes()));
    }

    private static byte[] make_unknown_reply()
    {
        ByteBuffer bb = ByteBuffer.allocate(50);
        return addChkSumAndETX( bb.put ( (byte)1 ).put( "9999".getBytes() ) );
    }

    static byte[] makeReply(FragmentActivity activity, String cmd_str )
    {
        if( cmd_str.startsWith( "i201" ) ) {
            return makeByChannelsReply(activity, cmd_str, new I201Reply() );
        } else if( cmd_str.startsWith( "i205" ) ) {
            return makeByChannelsReply(activity, cmd_str, new I205Reply() );
        } else if( cmd_str.startsWith("i214") || cmd_str.startsWith("i234") ) {
            return makeByChannelsReply(activity, cmd_str, new I214I234Reply() );
        } else if( cmd_str.equals("i90200") ) {
            return make_i902_reply();
        }
        return make_unknown_reply();
    }
}

public class Server {
    private final TanksView activity;
    private ServerSocket serverSocket;
    private boolean stopFlag = false;

    private static final int socketServerPORT = 10000;

    public Server(TanksView activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    private int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                stopFlag = true;
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT);

                while (!stopFlag) {
                    // block the call until connection is created and return
                    // Socket object
                    Socket socket = serverSocket.accept();
                    count++;

                    SocketServerReplyThread socketServerReplyThread =
                            new SocketServerReplyThread(socket, count);
//                    socketServerReplyThread.run();
                    socketServerReplyThread.start();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private final Socket hostThreadSocket;
        private final int cnt;
        String message;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            int req_no = 0;
            final String hostAndPort = hostThreadSocket.getInetAddress().getHostAddress() + ":" + + hostThreadSocket.getPort();
            try {
                InputStream inputStream = hostThreadSocket.getInputStream();

                while( !stopFlag ) {

                    int rc = inputStream.read();
                    if( -1 == rc )
                        break;
                    if( rc != 0x1 )
                        continue;

                    byte[] cmd = new byte[6];
                    if( 6 != inputStream.read(cmd) )
                        continue;

                    String cmd_str = new String(cmd);
                    req_no++;
                    byte[] reply = TLSProto.makeReply(activity, cmd_str);

                    message = "#" + cnt + "." + req_no + ". Src: " + hostAndPort + ", req: " + cmd_str + "\n";
                    if( null != reply )
                        hostThreadSocket.getOutputStream().write(reply);

                    activity.infoMsg(message);

//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            activity.infoMsg(message);
//                        }
//                    });
                }
                hostThreadSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                activity.infoMsg("Something wrong! " + e.toString() + "\n");
            }
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    activity.infoMsg( "#" + cnt + ". Host: " + hostAndPort + " conn closed\n" );
//                }
//            });
            activity.infoMsg("#" + cnt + ". Src: " + hostAndPort + " conn closed\n");
        }

    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        if( ip.isEmpty() )
                            ip = "Server running at : ";
                        else
                            ip += "\n";
                        ip += inetAddress.getHostAddress() + ":" + getPort();
                    }
                }
            }
            if(ip.isEmpty())
                ip = "*:" + getPort();

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}

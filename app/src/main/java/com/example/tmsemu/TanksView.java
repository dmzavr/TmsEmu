package com.example.tmsemu;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
//import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.dmzavr.MyTankView;
import com.google.dmzavr.Server;

public class TanksView extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener
{
    public TextView msg; //, infoip;
    Server server;

//    private SparseArray<MyTankView> tankViews = new SparseArray<>();

    public void infoMsg(String msg_)
    {
        class PutLogMsg implements Runnable {
            private final String msg_;
            private PutLogMsg(String m) {
                msg_ = String.valueOf(m);
            }

            @Override
            public void run() {
                CharSequence cseq = msg_ + msg.getText();
                msg.setText(cseq.subSequence(0, cseq.length() > 400 ? 400 : cseq.length() - 1));
            }
        }
        runOnUiThread( new PutLogMsg(msg_) );
    }

    @Override
    public void onListFragmentInteraction(Integer channelId) {
        editTank( null == channelId ? -1 : channelId );
    }

    @Override
    public void onTankListSizeChanged(int newsize) {
        TextView addTankText = findViewById(R.id.textViewAddTank);
        addTankText.setVisibility( newsize >=8 ? View.GONE : View.VISIBLE );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tanks_view);
        msg = findViewById(R.id.msg);
        msg.setMovementMethod(new ScrollingMovementMethod());
//        infoip = findViewById(R.id.infoip);
        init();

        server = new Server(this);
//        infoip.setText(server.getIpAddress() + ":" + server.getPort());
        Toast toast = Toast.makeText( getApplicationContext(), server.getIpAddress(), Toast.LENGTH_LONG );
        toast.show();
    }

    @Override
    protected void onDestroy() {
        server.onDestroy();
        super.onDestroy();
    }

    private void init() {
        TextView addTankText = findViewById(R.id.textViewAddTank);
        addTankText.setPaintFlags(addTankText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
/*        ScrollView sv = findViewById(R.id.scrollView2);
        LinearLayout tl = findViewById(R.id.linearLayout);

        for( int i = 1; i < 6; ++i ) {
            MyTankView tv = new MyTankView(this);
            tv.setChannel(i);
            tv.setPadding(10, 10, 10, 10);
            tl.addView( tv );
            tankViews.put(i, tv);
        }
*/
    }

    public void onAddTankClick(android.view.View text) {
        editTank(-1);
    }

    private void editTank( int channelId )
    {
        Intent intent = new Intent( this, TankEditorActivity.class);
        intent.putExtra("ChannelId", channelId );
        startActivity(intent);
    }

}

package com.creativethoughts.iscore.custom_alert_dialogs;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativethoughts.iscore.FragmentMenuCard;
import com.creativethoughts.iscore.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlertMessageFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private static final String KEY_VALUE = "keyvalue";
    private static final String TITLE = "title";
    private static final String HAPPY = "happy";
    private static final String MESSAGE = "message";

    public AlertMessageFragment() {
        // Required empty public constructor
    }
    public static AlertMessageFragment getInstance(ArrayList<KeyValuePair> keyValuePairs, String title,String message, boolean isHappy, boolean isBackButtonEnable ){
        AlertMessageFragment alertMessageFragment = new AlertMessageFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList( KEY_VALUE, keyValuePairs );
        bundle.putString( TITLE, title );
        bundle.putBoolean( HAPPY, isHappy );
        bundle.putString( MESSAGE, message );
        alertMessageFragment.setArguments(bundle);

        return alertMessageFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_alert, container, false);
        view.findViewById( R.id.rltv_footer ).setOnClickListener( view1 -> {
            try{
                getFragmentManager().beginTransaction().replace( R.id.container, FragmentMenuCard.newInstance("EMPTY","EMPTY") )
                        .commit();
            }catch ( NullPointerException e ){
                //Do nothing
            }
        } );
        mRecyclerView = view.findViewById( R.id.recycler_message );
        ImageView imgIcon      = view.findViewById( R.id.img_success );
        TextView txtTitle       = view.findViewById( R.id.txt_success );
        TextView txtMessage = view.findViewById( R.id.txt_message );
        try{
            Bundle bundle = getArguments();
            boolean isHappy = bundle.getBoolean( HAPPY );
            String title = bundle.getString( TITLE );
            String message = bundle.getString( MESSAGE );
            txtMessage.setText( message );
            txtTitle.setText( title );
            if ( !isHappy ){
                imgIcon.setImageResource( R.mipmap.ic_failed );
            }
            ArrayList<KeyValuePair> keyValuePairs = bundle.getParcelableArrayList( KEY_VALUE );
            SuccessAdapter successAdapter = new SuccessAdapter( keyValuePairs );
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getContext() );
            mRecyclerView.setLayoutManager( layoutManager );
            mRecyclerView.setAdapter( successAdapter );
        }catch ( Exception e){
            //Do nothing
        }
        return view;
    }

}

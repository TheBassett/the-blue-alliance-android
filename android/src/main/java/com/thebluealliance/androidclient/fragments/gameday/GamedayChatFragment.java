package com.thebluealliance.androidclient.fragments.gameday;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.sorcix.sirc.Channel;
import com.thebluealliance.androidclient.Constants;
import com.thebluealliance.androidclient.R;
import com.thebluealliance.androidclient.TBAAndroid;
import com.thebluealliance.androidclient.Utilities;
import com.thebluealliance.androidclient.di.components.DaggerGamedayChatComponent;
import com.thebluealliance.androidclient.di.components.GamedayChatComponent;
import com.thebluealliance.twitch.TwitchChatListener;
import com.thebluealliance.twitch.OAuthController;
import com.thebluealliance.twitch.RxTwitchChat;
import com.thebluealliance.twitch.TwitchChatController;
import com.wuman.android.auth.OAuthManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GamedayChatFragment extends Fragment {

    @Inject TwitchChatController mChatController;
    @Inject RxTwitchChat mRxChat;
    @Inject TwitchChatListener mChatListener;

    private OAuthController mOauthController;
    private Channel mChannel;

    public static GamedayChatFragment newInstance() {
        return new GamedayChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        String clientId = Utilities.readLocalProperty(getActivity(), "twitch.clientId");
        String clientSecret = Utilities.readLocalProperty(getActivity(), "twitch.secret");
        String redirect = "http://localhost";
        List<String> scopes = new ArrayList<>();
        scopes.add("chat_login");
        mOauthController = OAuthController.newInstance(
                getActivity().getApplicationContext(),
                getActivity().getFragmentManager(),
                new ClientParametersAuthentication(clientId, clientSecret),
                "https://api.twitch.tv/kraken/oauth2/authorize",
                "https://api.twitch.tv/kraken/oauth2/authorize",
                redirect,
                scopes
        );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gameday_chat, null);
        Button login = (Button) v.findViewById(R.id.twitch_login_button);
        login.setOnClickListener(v1 -> Observable.defer(() -> Observable.just(getOauth()))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(credentialOAuthFuture -> {
                    try {
                        String token = credentialOAuthFuture.getResult().getAccessToken();
                        Log.d(Constants.LOG_TAG, "Got twitch token: " + token);

                        //TODO don't hardcode username
                        mChannel = mChatController.connectToGameday("plnyyanks", token, mRxChat);
                        mRxChat.getObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(mChatListener);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatController.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChannel != null) {
            mChannel.join();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mChannel != null) {
            mChannel.part();
        }
    }

    private OAuthManager.OAuthFuture<Credential> getOauth() {
        //TODO don't hardcode username
        // You'll have to read it in from a textbox at the top here, or something
        // I don't think we can get it back from the oauth flow
        return mOauthController.authorizeImplicitly("plnyyanks");
    }

    private GamedayChatComponent getComponent() {
        return DaggerGamedayChatComponent.builder()
                .gamedayChatModule(((TBAAndroid)getActivity().getApplication()).getGamedayChatModule())
                .build();
    }
}

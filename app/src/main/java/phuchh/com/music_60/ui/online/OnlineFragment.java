package phuchh.com.music_60.ui.online;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import phuchh.com.music_60.R;
import phuchh.com.music_60.adapter.GenreAdapter;
import phuchh.com.music_60.adapter.TopchartAdapter;
import phuchh.com.music_60.data.model.Track;
import phuchh.com.music_60.data.source.TrackRepository;
import phuchh.com.music_60.data.source.local.TrackLocalDataSource;
import phuchh.com.music_60.data.source.remote.TrackRemoteDataSource;
import phuchh.com.music_60.service.PlayMusicService;
import phuchh.com.music_60.ui.listtrack.ListTrackActivity;
import phuchh.com.music_60.utils.Constant;

import static phuchh.com.music_60.service.PlayMusicService.getMyServiceIntent;

public class OnlineFragment extends Fragment implements OnlineContract.View,
        TopchartAdapter.TopchartOnClickListener, GenreAdapter.GenreOnClickListener {
    public static final int SPAN_COUNT = 3;
    private OnlineContract.Presenter mPresenter;
    private RecyclerView mRecyclerLatest;
    private RecyclerView mRecyclerGenre;
    private ServiceConnection mConnection;
    private PlayMusicService mService;
    private List<Track> mTracks;

    public static OnlineFragment newInstance() {
        return new OnlineFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        boundToService();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment();
        mPresenter.getTopChart(Constant.ALL_MUSIC, Constant.LIMIT_DEFAULT, Constant.OFFSET_DEFAULT);
        showGenres();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
    }

    @Override
    public void showFailedMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showTopChart(List<Track> tracks) {
        mTracks = tracks;
        TopchartAdapter adapter = new TopchartAdapter(getActivity(), tracks);
        adapter.setTopchartListener(this);
        mRecyclerLatest.setAdapter(adapter);
        mRecyclerLatest.setLayoutManager(new GridLayoutManager(getActivity(), SPAN_COUNT));
    }

    @Override
    public void onGenreClick() {
       Intent intent = new Intent(getActivity(), ListTrackActivity.class);
       startActivity(intent);
    }

    @Override
    public void onTopchartClick(int index) {
        mService.setTracks(mTracks);
        mService.createTrack(index);
    }
    private void initFragment() {
        mRecyclerLatest = getView().findViewById(R.id.recycler_latest);
        mRecyclerGenre = getView().findViewById(R.id.recycler_genres);
        mPresenter = new OnlinePresenter(this,
                TrackRepository.getInstance(TrackRemoteDataSource.getInstance(),
                        TrackLocalDataSource.getInstance()));
    }

    private void showGenres() {
        GenreAdapter adapter = new GenreAdapter(getActivity());
        adapter.setGenreListener(this);
        mRecyclerGenre.setAdapter(adapter);
        mRecyclerGenre.setLayoutManager(new GridLayoutManager(getActivity(), SPAN_COUNT));
    }

    private void boundToService() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                PlayMusicService.LocalBinder binder = (PlayMusicService.LocalBinder) iBinder;
                mService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                getActivity().unbindService(mConnection);
            }
        };
        getActivity().bindService(getMyServiceIntent(getActivity()), mConnection, Context.BIND_AUTO_CREATE);
    }
}

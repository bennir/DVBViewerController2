package de.bennir.dvbviewercontroller2.service;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.model.EpgInfo;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface EpgService {
    @GET("/Epg/{id}")
    void getEpg(@Path("id") String channelId, @Query("time") String time, Callback<ArrayList<EpgInfo>> cb);
}

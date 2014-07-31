package de.bennir.dvbviewercontroller2.service;

import de.bennir.dvbviewercontroller2.model.DVBCommand;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface CommandService {
    @POST("/Command")
    void sendCommand(@Body DVBCommand command, Callback<DVBCommand> cb);
}

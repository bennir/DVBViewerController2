package de.bennir.dvbviewercontroller2.interfaces;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.model.Channel;

public interface SetChannelList {
    public void onSetChannelListener(ArrayList<Channel> channels);
}

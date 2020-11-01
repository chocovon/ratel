package org.nico.ratel.landlords.client.event;

import io.netty.channel.Channel;
import org.nico.ratel.landlords.print.SimplePrinter;

public class ClientEventListener_CODE_GAME_PLAY_TIME_OUT extends ClientEventListener{

    @Override
    public void call(Channel channel, String data) {

        SimplePrinter.printNotice("Operation timed out, please wait for another turn...");

    }

}
package org.nico.ratel.landlords.server.timer;

import org.nico.ratel.landlords.channel.ChannelUtils;
import org.nico.ratel.landlords.entity.ClientSide;
import org.nico.ratel.landlords.entity.PokerSell;
import org.nico.ratel.landlords.entity.Room;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.nico.ratel.landlords.enums.RoomStatus;
import org.nico.ratel.landlords.enums.ServerEventCode;
import org.nico.ratel.landlords.helper.PokerHelper;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.robot.RobotDecisionMakers;
import org.nico.ratel.landlords.server.event.ServerEventListener;

import java.util.TimerTask;

public class RoomTimer extends TimerTask {
    final private Room room;

    public RoomTimer(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        try {
            doing();
        }catch(Exception e) {
            SimplePrinter.serverLog(e.getMessage());
        }
    }

    public void doing() {
        if (room.getStatus() == RoomStatus.STARTING) {
            for (ClientSide clientSide : room.getClientSideList()) {
                if (clientSide.getId() == room.getCurrentSellClient()) {
                    if (clientSide.getActionSecLeft() == -1) {
                        clientSide.setActionSecLeft(room.getActionTimeLimit());
                    }

                    if (clientSide.getActionSecLeft() > 0) {
                        clientSide.setActionSecLeft(clientSide.getActionSecLeft() - 1);
                    } else {
                        clientSide.setActionSecLeft(room.getActionTimeLimit());
                        ChannelUtils.pushToClient(clientSide.getChannel(), ClientEventCode.CODE_GAME_PLAY_TIME_OUT, null);
                        if (room.getLandlordId() == -1) {
                            ServerEventListener.get(ServerEventCode.CODE_GAME_LANDLORD_ELECT).call(clientSide, String.valueOf(false));
                        } else if (room.getLastSellClient() == room.getCurrentSellClient()) {
                            PokerSell pokerSell = RobotDecisionMakers.howToPlayPokers(RobotDecisionMakers.SIMPLE, null, clientSide.getPokers());
                            ServerEventListener.get(ServerEventCode.CODE_GAME_POKER_PLAY).call(clientSide, PokerHelper.toJsonData(pokerSell));
                        } else {
                            ServerEventListener.get(ServerEventCode.CODE_GAME_POKER_PLAY_PASS).call(clientSide, null);
                        }
                    }
                } else {
                    clientSide.setActionSecLeft(room.getActionTimeLimit());
                }
            }
        }
    }
}

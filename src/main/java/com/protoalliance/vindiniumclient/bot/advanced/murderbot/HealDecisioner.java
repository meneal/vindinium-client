package com.protoalliance.vindiniumclient.bot.advanced.murderbot;

import com.protoalliance.vindiniumclient.bot.BotMove;
import com.protoalliance.vindiniumclient.bot.BotUtils;
import com.protoalliance.vindiniumclient.bot.advanced.Pub;
import com.protoalliance.vindiniumclient.dto.GameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Decides the best way to get healed.
 *
 * This decisioner will do its best to steer the bot towards a tavern without confrontation.
 *
 * On the Maslow Hierarchy, this falls under safety.
 */
public class HealDecisioner implements Decision<AdvancedMurderBot.GameContext, BotMove> {

    private static final Logger logger = LogManager.getLogger(HealDecisioner.class);

    @Override
    public BotMove makeDecision(AdvancedMurderBot.GameContext context) {
        logger.info("Need to heal; running to nearest pub.");

        Map<GameState.Position, AdvancedMurderBot.DijkstraResult> dijkstraResultMap = context.getDijkstraResultMap();

        // Run to the nearest pub
        Pub nearestPub = null;
        AdvancedMurderBot.DijkstraResult nearestPubDijkstraResult = null;
        for(Pub pub : context.getGameState().getPubs().values()) {
            AdvancedMurderBot.DijkstraResult dijkstraToPub = dijkstraResultMap.get(pub.getPosition());
            if(dijkstraToPub != null) {
                if(nearestPub == null || nearestPubDijkstraResult.getDistance() >
                    dijkstraToPub.getDistance()) {
                    nearestPub = pub;
                    nearestPubDijkstraResult = dijkstraResultMap.get(pub.getPosition());
                }
            }
        }

        if(nearestPub == null)
            return BotMove.STAY;

        GameState.Position nextMove = nearestPub.getPosition();
        while(nearestPubDijkstraResult.getDistance() > 1) {
            nextMove = nearestPubDijkstraResult.getPrevious();
            nearestPubDijkstraResult = dijkstraResultMap.get(nextMove);
        }

        return BotUtils.directionTowards(nearestPubDijkstraResult.getPrevious(), nextMove);
    }
}

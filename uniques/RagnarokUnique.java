/*
#
# This file is part of aion-lightning <aion-lightning.org>.
#
# aion-lightning is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# aion-lightning is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
#
 */


package com.aionemu.gameserver.services.uniques;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.BossesConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Erazer GlobalXcentra
 */
public class RagnarokUnique {
	private static final Logger log = LoggerFactory.getLogger("UNIQUES_LOG");
	private static List<float[]> floatArray = new ArrayList<float[]>();
	private static final String RAGNAROK_SPAWN_SCHEDULE = BossesConfig.RAGNAROK_SPAWN_SCHEDULE;
	private static int WORLD_ID = 400010000;
	private static int NPC_ID = 284021;
	private static int[] rewards = {186000230};
        private static Npc mainN;

        public static void ScheduleCron(){
             CronService.getInstance().schedule(new Runnable(){

                  @Override
                  public void run() {
                       startEvent(); //To change body of generated methods, choose Tools | Templates.
                  }

             },RAGNAROK_SPAWN_SCHEDULE);
        }

        public static void startEvent(){
                initCoordinates();

                World.getInstance().doOnAllPlayers(new Visitor<Player>(){

                        @Override
                        public void visit(Player object) {
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Ragnarok has appeared somewhere in Eye of Reshanta");
								log.info("Ragnarok has appeared somewhere in Eye of Reshanta\n");
                        }
                });

                initRagnarok();

                ThreadPoolManager.getInstance().schedule(new Runnable(){

                     @Override
                     public void run() {
                          endEvent(); //To change body of generated methods, choose Tools | Templates.
                     }
                }, BossesConfig.DESPAWN * 60 * 1000);

        }

        private static void initRagnarok() {
                float[] coords = floatArray.get(Rnd.get(floatArray.size()));
                SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(WORLD_ID, NPC_ID, coords[0], coords[1], coords[2], (byte) coords[3]);
                VisibleObject mainObject = SpawnEngine.spawnObject(spawn, 1);
                if(mainObject instanceof Npc) {
                      mainN = (Npc) mainObject;
                }
                ActionObserver observer = new ActionObserver(ObserverType.DEATH){

                        @Override
                        public void died(Creature creature) {
                                if(creature instanceof Player) {
                                        final Player player = (Player) creature;
                                        final int id = rewards[Rnd.get(rewards.length)];
                                        ItemService.addItem(player, id, BossesConfig.RAGNAROK_EVENT_COUNT_REWARD);
                                        AbyssPointsService.addGp(player, BossesConfig.RAGNAROK_GP);
                                        AbyssPointsService.addAp(player, BossesConfig.RAGNAROK_AP);
                                        World.getInstance().doOnAllPlayers(new Visitor<Player>(){

                                                @Override
                                                public void visit(Player object) {
                                                        PacketSendUtility.sendBrightYellowMessageOnCenter(object, player.getName() +  " killed Ragnarok from Eye of Reshanta");
														log.info("Ragnarok got killed from " + player.getName() + ".\n");
                                                }
                                        });
                                }
                                mainN.getObserveController().removeObserver(this);
                                mainN.setSpawn(null);
                                mainN.getController().onDelete();
                        }
                };
                if(mainN != null) {
                        mainN.getObserveController().attach(observer);
                }
        }

        public static void endEvent(){
                World.getInstance().doOnAllPlayers(new Visitor<Player>(){

                        @Override
                        public void visit(Player object) {
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Ragnarok from Eye of Reshanta disappeared");
								log.info("Ragnarok just disappeared\n");
                        }
                });

                mainN.getController().onDelete();
        }

        private static void initCoordinates(){
				floatArray.add(new float[] { 1876.5498f, 2017.016f, 2277.2617f, (byte) 108} );
				floatArray.add(new float[] { 1934.9397f, 1706.3125f, 2164.5425f, (byte) 10} );
				floatArray.add(new float[] { 2419.7246f, 1940.1924f, 2210.996f, (byte) 52} );
				floatArray.add(new float[] { 1975.4418f, 1952.8756f, 2259.0735f, (byte) 54} );
				floatArray.add(new float[] { 2041.4915f, 1758.9154f, 2215.1733f, (byte) 110} );
				floatArray.add(new float[] { 2164.1958f, 2188.0703f, 2338.062f, (byte) 87} );
        }
}
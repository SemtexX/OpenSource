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
public class YamennesUnique {
	private static final Logger log = LoggerFactory.getLogger("UNIQUES_LOG");
	private static List<float[]> floatArray = new ArrayList<float[]>();
	private static final String YAMENNES_SPAWN_SCHEDULE = BossesConfig.YAMENNES_SPAWN_SCHEDULE;
	private static int WORLD_ID = 600100000;
	private static int NPC_ID = 219555;
	private static int[] rewards = {186000230};
        private static Npc mainN;

        public static void ScheduleCron(){
             CronService.getInstance().schedule(new Runnable(){

                  @Override
                  public void run() {
                       startEvent(); //To change body of generated methods, choose Tools | Templates.
                  }

             },YAMENNES_SPAWN_SCHEDULE);
        }

        public static void startEvent(){
                initCoordinates();

                World.getInstance().doOnAllPlayers(new Visitor<Player>(){

                        @Override
                        public void visit(Player object) {
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Yamennes has appeared somewhere in Levinshor");
								log.info("Yamennes has appeared somewhere in Levinshor\n");
                        }
                });

                initYamennes();

                ThreadPoolManager.getInstance().schedule(new Runnable(){

                     @Override
                     public void run() {
                          endEvent(); //To change body of generated methods, choose Tools | Templates.
                     }
                }, BossesConfig.DESPAWN * 60 * 1000);

        }

        private static void initYamennes() {
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
                                        ItemService.addItem(player, id, BossesConfig.YAMENNES_EVENT_COUNT_REWARD);
                                        AbyssPointsService.addGp(player, BossesConfig.YAMENNES_GP);
                                        AbyssPointsService.addAp(player, BossesConfig.YAMENNES_AP);
                                        World.getInstance().doOnAllPlayers(new Visitor<Player>(){

                                                @Override
                                                public void visit(Player object) {
                                                        PacketSendUtility.sendBrightYellowMessageOnCenter(object, player.getName() +  " killed Yamennes from Levinshor");
														log.info("Yamennes got killed from " + player.getName() + ".\n");
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
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Yamennes from Levinshor disappeared");
								log.info("Yamennes just disappeared\n");
                        }
                });

                mainN.getController().onDelete();
        }

        private static void initCoordinates(){
				floatArray.add(new float[] { 390.38095f, 1820.0548f, 226.5f, (byte) 89} );
				floatArray.add(new float[] { 1414.6161f, 1045.1346f, 273.2501f, (byte) 117} );
				floatArray.add(new float[] { 1153.4858f, 972.8595f, 311.125f, (byte) 25} );
				floatArray.add(new float[] { 676.0082f, 999.4389f, 274.62552f, (byte) 66} );
        }
}
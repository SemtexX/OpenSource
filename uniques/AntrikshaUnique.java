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
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.knownlist.Visitor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Erazer GlobalXcentra
 */
public class AntrikshaUnique {
	private static final Logger log = LoggerFactory.getLogger("UNIQUES_LOG");
	private static List<float[]> floatArray = new ArrayList<float[]>();
	private static final String ANTRIKSHA_SPAWN_SCHEDULE = BossesConfig.ANTRIKSHA_SPAWN_SCHEDULE;
	private static int WORLD_ID = 600090000;
	private static int NPC_ID = 277224;
	private static int[] rewards = {186000230};
    private static Npc mainN;

        public static void ScheduleCron(){
             CronService.getInstance().schedule(new Runnable(){

                  @Override
                  public void run() {
                       startEvent(); //To change body of generated methods, choose Tools | Templates.
                  }

             },ANTRIKSHA_SPAWN_SCHEDULE);
        }

        public static void startEvent(){
                initCoordinates();

                World.getInstance().doOnAllPlayers(new Visitor<Player>(){

                        @Override
                        public void visit(Player object) {
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Antriksha has appeared somewhere in Kaldor");
								log.info("Antriksha has appeared somewhere in Kaldor\n");
                        }
                });

                initAntriksha();

                ThreadPoolManager.getInstance().schedule(new Runnable(){

                     @Override
                     public void run() {
                          endEvent(); //To change body of generated methods, choose Tools | Templates.
                     }
                }, BossesConfig.DESPAWN * 60 * 1000);

        }

        private static void initAntriksha() {
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
                                        ItemService.addItem(player, id, BossesConfig.ANTRIKSHA_EVENT_COUNT_REWARD);
                                        AbyssPointsService.addGp(player, BossesConfig.ANTRIKSHA_GP);
                                        AbyssPointsService.addAp(player, BossesConfig.ANTRIKSHA_AP);
                                        World.getInstance().getWorldMap(WorldMapType.LDF5_FORTRESS.getId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>(){

                                                @Override
                                                public void visit(Player object) {
                                                        PacketSendUtility.sendBrightYellowMessageOnCenter(object, player.getName() +  " killed Antriksha from Kaldor");
														log.info(" Antriksha got killed from " + player.getName() + ".\n");
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
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Antriksha from Kaldor disappeared");
								log.info("Antriksha just disappeared\n");
                        }
                });

                mainN.getController().onDelete();
        }

        private static void initCoordinates(){
				floatArray.add(new float[] { 285.60645f, 509.25003f, 158.125f, (byte) 115} );
				floatArray.add(new float[] { 1199.3313f, 353.66617f, 128.375f, (byte) 72} );
				floatArray.add(new float[] { 628.1784f, 1358.4932f, 150.41344f, (byte) 90} );
				floatArray.add(new float[] { 933.3529f, 1232.8959f, 143.7078f, (byte) 77} );
        }
}
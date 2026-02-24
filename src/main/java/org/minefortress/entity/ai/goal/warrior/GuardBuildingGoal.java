package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.fortress.ServerFortressManager;
import org.minefortress.fortress.buildings.ClientBuildingsManager;
import org.minefortress.fortress.buildings.FortressBuildingManager;

import java.io.Console;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GuardBuildingGoal extends AttackGoal {

    private static final int UPDATE_INTERVAL = 40; // Check for tower every 2 seconds
    private int checkTicker = 0;
    private BlockPos targetTowerPos = null;

    public static Set<Vec3d> occupied = new HashSet<>();

    public GuardBuildingGoal(BasePawnEntity pawn) {
        super(pawn);
    }

    private static BlockPos findPlankPlot(IFortressBuilding building, World world, BasePawnEntity pawn, GuardBuildingGoal goal) {

        BlockPos start = building.getStart();
        BlockPos end = building.getEnd();

        // Iterate through every block in the building's bounding box
        for (BlockPos pos : BlockPos.iterate(start, end)) {
            BlockState state = world.getBlockState(pos);

            // Check if the block is any type of wooden plank
            if (state.isIn(BlockTags.PLANKS)) {
                // We found the floor! Return the space above it
                BlockPos standPos = pos.up();

                // Optional: Ensure the space above is actually clear (air)
                if (world.getBlockState(standPos).isAir() && !occupied.contains(standPos.toCenterPos())) {

                    Box occupationBox = new Box(standPos).expand(0.2, 0.5, 0.2);
                    List<BasePawnEntity> others = world.getEntitiesByClass(
                            BasePawnEntity.class,
                            occupationBox,
                            entity -> entity != pawn
                    );

                    if (others.isEmpty()) {
                        Logger.getLogger(GuardBuildingGoal.class.getSimpleName()).info("Free " + standPos.toShortString());
                        return standPos; // Nobody is here, take the spot!
                    }
                    else
                    {
                        goal.targetTowerPos = null;
                        Logger.getLogger(GuardBuildingGoal.class.getSimpleName()).info("Occupied by " + others.get(0).getName().getContent().toString());
                    }
                }
                else
                {
                    Logger.getLogger(GuardBuildingGoal.class.getSimpleName()).info("Occupied by someone");
                }
            }
        }
        return null;
    }

    @Override
    public boolean canStart() {
        if (this.pawn.getTarget() != null) return false;

        if(this.targetTowerPos == null) {
            Logger.getLogger(this.getClass().getSimpleName()).info("Looking for defense-tower!");
            this.targetTowerPos = findNearestDefenseTower();
            if (this.targetTowerPos != null) {
                if(occupied.add(targetTowerPos.toCenterPos())) {
                    this.pawn.getServer().getPlayerManager().broadcast(
                            Text.literal(pawn.getName().getString() + " is going to defense tower! " + targetTowerPos.toShortString()),
                            false
                    );
                }
                else
                {
                    this.targetTowerPos = null;
                }
            }
        }
        return this.targetTowerPos != null;
    }

    private Vec3d lastPos;

    @Override
    public void tick() {
        checkTicker--;

        // 1. Find the nearest tower if we don't have one or every interval
        if (checkTicker <= 0) {
            checkTicker = UPDATE_INTERVAL;

            //this.targetTowerPos = findNearestDefenseTower();

        }


        // 2. If we have a tower position, move toward it
        if (targetTowerPos != null) {

            double distanceSq = this.pawn.squaredDistanceTo(targetTowerPos.getX(), targetTowerPos.getY(), targetTowerPos.getZ());

            // If further than 2 blocks from the center of the tower, move to it
            if (distanceSq > 1.0D) {
                //Logger.getLogger(this.getClass().getSimpleName()).
                        //info("Position dist:" + distanceSq + " --> " + pawn.getName().getString() + " " + pawn.getPos().toString());
                if(distanceSq < 100.0 && distanceSq > 2.0)
                {
                    Vec3d pos = pawn.getPos();
                    if(lastPos != null)
                    {
                        if(lastPos.distanceTo(pos) < 0.2)
                        {
                            Logger.getLogger(this.getClass().getSimpleName()).
                                    info("Teleport " + pawn.getName().getString() + " " + pawn.getPos().toString()
                                            + " to " + targetTowerPos.toShortString());
                             //Stuck
                            pawn.setPos(targetTowerPos.getX(), targetTowerPos.getY(), targetTowerPos.getZ());
                        }
                    }

                    lastPos = pos;
                }
                this.pawn.getNavigation().startMovingTo(
                        targetTowerPos.getX(),
                        targetTowerPos.getY(),
                        targetTowerPos.getZ(),
                        3.0D // Movement speed multiplier
                );
            }
            else {
                // We are at the tower, stay put and look around
                this.pawn.getNavigation().stop();
            }
        }
    }

    private BlockPos findNearestDefenseTower() {
        FortressBuildingManager buildingManager = FortressBuildingManager.instance;

        // 3. Find all buildings and filter for defense towers
        // Note: Adjust "defense" or "small_defense_tower" based on your exact requirement
        Logger.getLogger(this.getClass().getSimpleName()).info(buildingManager.getAllBuildings().stream().map(x -> x.getMetadata().getId()).collect(Collectors.joining(", ")));
        List<IFortressBuilding> b = buildingManager.getAllBuildings()
                .stream()
                .filter(building -> "small_defense_tower".equals(building.getMetadata().getId()))
                // 2. Only include towers with fewer than 4 archers inside
                .filter(building -> {
                    Box buildingBox = new Box(building.getStart(), building.getEnd());
                    List<BasePawnEntity> archersInside = pawn.getEntityWorld().getEntitiesByClass(
                            BasePawnEntity.class,
                            buildingBox,
                            entity -> true
                    );
                    return archersInside.size() < 4;
                })
                .sorted(Comparator.comparingDouble(building -> 
            this.pawn.getBlockPos().getSquaredDistance(building.getStart()) // Use .getStart() or .getPos()
        ))
        .collect(Collectors.toList());
        if(b.isEmpty())
        {
            //Logger.getLogger(this.getClass().getSimpleName()).info("no small_defense_tower found!");
            //Text.literal(pawn.getName().getString() + " no small_defense_tower found!");
            return null;
        }
        
        for (IFortressBuilding building : b) {
            BlockPos pos = findPlankPlot(building, pawn.getWorld(), pawn, this);
            if(pos != null)
            {
                Logger.getLogger(this.getClass().getSimpleName()).info("found at "+ building.getPos().toShortString());
                return pos;
            }
        }
        
        return null;
    }


    @Override
    public boolean shouldContinue() {
        // Keep guarding unless the pawn is told to do something else by the player
        return pawn.getTarget() == null && targetTowerPos != null;
    }

    @Override
    public void stop() {
        super.stop();
        this.pawn.getNavigation().stop();
    }
}


package com.github.thierryreding.mcpf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlserverevents.FMLServerStoppedEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dosse.upnp.Gateway;
import com.dosse.upnp.GatewayFinder;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mcpf")
public class PortForward extends GatewayFinder
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private Gateway gateway;
    private int port = 0;

    public PortForward() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void gatewayFound(Gateway gateway) {
        if (this.gateway == null) {
            this.gateway = gateway;
        }
    }

    public boolean hasGateway() {
        return this.gateway != null;
    }

    public Gateway getGateway() {
        return this.gateway;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @SubscribeEvent
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (this.port > 0) {
            LOGGER.info("removing port forward for {}", this.port);
            this.gateway.closePort(this.port,  false);
        }
    }

    @SubscribeEvent
    public void replaceGui(GuiOpenEvent event) {
        Screen gui = event.getGui();

        if (gui instanceof net.minecraft.client.gui.screens.ShareToLanScreen) {
            Minecraft minecraft = Minecraft.getInstance();
            gui = new ShareToLanScreen(minecraft.screen, this);
            event.setGui(gui);
        }
    }
}

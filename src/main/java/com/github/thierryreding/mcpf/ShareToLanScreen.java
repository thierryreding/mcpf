package com.github.thierryreding.mcpf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dosse.upnp.Gateway;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class ShareToLanScreen extends net.minecraft.client.gui.screens.ShareToLanScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component ALLOW_COMMANDS_LABEL = new TranslatableComponent("selectWorld.allowCommands");
    private static final Component GAME_MODE_LABEL = new TranslatableComponent("selectWorld.gameMode");
    private GameType gameMode = GameType.SURVIVAL;
    private final PortForward portForward;
    private final Screen parent;
    private boolean forwardPort;
    private boolean commands;

    public ShareToLanScreen(Screen parent, PortForward portForward) {
        super(parent);
        this.portForward = portForward;
        this.parent = parent;
    }

    @Override
    protected void init() {
        AbstractButton widget;

        widget = CycleButton.builder(GameType::getShortDisplayName).withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE).withInitialValue(this.gameMode).create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (eventListener, value) -> {
            this.gameMode = value;
        });
        this.addRenderableWidget(widget);

        widget = CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (eventListener, value) -> {
            this.commands = value;
        });
        this.addRenderableWidget(widget);

        widget = CycleButton.onOffBuilder(this.forwardPort).create(this.width / 2 - 155, 125, 150, 20, new TranslatableComponent("lanServer.portForward"), (eventListener, value) -> {
            this.forwardPort = value;
        });
        /* disable button if UPnP is not supported */
        widget.active = this.portForward != null;
        this.addRenderableWidget(widget);

        widget = new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("lanServer.start"), (eventListener) -> {
            this.minecraft.setScreen((Screen)null);
            int port = HttpUtil.getAvailablePort();
            Component component;

            if (this.portForward.hasGateway()) {
                Gateway gateway = this.portForward.getGateway();
                String ip = gateway.getExternalIP();

                if (!gateway.isMapped(port, false)) {
                    LOGGER.info("forwarding port {} for local IP {}", port, gateway.getLocalIP());
                    if (!gateway.openPort(port, false, "Minecraft")) {
                        LOGGER.error("failed to forward port {} for local IP {}", port, gateway.getLocalIP());
                    } else {
                        this.portForward.setPort(port);
                    }
                } else {
                    LOGGER.info("port {} already forwarded", port);
                }

                this.minecraft.keyboardHandler.setClipboard(String.format("%s:%d",  ip, port));
            }

            if (this.minecraft.getSingleplayerServer().publishServer(this.gameMode, this.commands, port)) {
                component = new TranslatableComponent("commands.publish.started", port);
            } else {
                component = new TranslatableComponent("commands.publish.failed");
            }

            this.minecraft.gui.getChat().addMessage(component);
            this.minecraft.updateTitle();
        });
        this.addRenderableWidget(widget);

        widget = new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, (eventListener) -> {
            this.minecraft.setScreen(this.parent);
        });
        this.addRenderableWidget(widget);
    }
}

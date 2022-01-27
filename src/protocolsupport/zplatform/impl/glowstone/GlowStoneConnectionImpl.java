package protocolsupport.zplatform.impl.glowstone;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.zplatform.impl.glowstone.network.GlowStoneChannelHandlers;
import protocolsupport.zplatform.network.NetworkManagerWrapper;

public class GlowStoneConnectionImpl extends ConnectionImpl {

	public GlowStoneConnectionImpl(NetworkManagerWrapper networkmanager) {
		super(networkmanager);
	}

	@Override
	public void receivePacket(final Object packet) {
		Runnable packetRecv = new Runnable() {
			public void run() {
				try {
					ChannelHandlerContext networkManagerContext = networkmanager.getChannel().pipeline().context(GlowStoneChannelHandlers.NETWORK_MANAGER);
					ChannelInboundHandler networkManager = (ChannelInboundHandler) networkManagerContext.handler();
					networkManager.channelRead(networkManagerContext, packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		if (networkmanager.getChannel().eventLoop().inEventLoop()) {
			packetRecv.run();
		} else {
			networkmanager.getChannel().eventLoop().submit(packetRecv);
		}
	}

	@Override
	public void sendPacket(final Object packet) {
		Runnable packetSend = new Runnable() {
			public void run() {
				try {
					ChannelHandlerContext encoderContext = networkmanager.getChannel().pipeline().context(GlowStoneChannelHandlers.DECODER_ENCODER);
					ChannelOutboundHandler encoderChannelHandler = (ChannelOutboundHandler) encoderContext.handler();
					encoderChannelHandler.write(encoderContext, packet, encoderContext.voidPromise());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		if (networkmanager.getChannel().eventLoop().inEventLoop()) {
			packetSend.run();
		} else {
			networkmanager.getChannel().eventLoop().submit(packetSend);
		}
	}

}

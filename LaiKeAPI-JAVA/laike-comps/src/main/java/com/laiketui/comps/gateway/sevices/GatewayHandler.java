package com.laiketui.comps.gateway.sevices;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

@Component
public class GatewayHandler extends SimpleChannelInboundHandler<Object>
{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {

    }


}

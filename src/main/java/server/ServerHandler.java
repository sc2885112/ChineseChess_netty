package server;

import enu.ClientMenu;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message){
        if(message.startsWith(ClientMenu.REQUEST_CONNECTION)) ServerInteractiveUitl.requestConnection(ctx,message);//请求连接
        if(message.startsWith(ClientMenu.LAUNCH_CHALLENGE))ServerInteractiveUitl.launchChallenge(message);//发起挑战
        if(message.startsWith(ClientMenu.ACCEPT_CHALLENGE))ServerInteractiveUitl.acceptChallenge(message);//接受挑战
        if(message.startsWith(ClientMenu.REFUSE_CHALLENGE))ServerInteractiveUitl.refuseChallenge(message);//拒绝挑战
        if(message.startsWith(ClientMenu.ADMIT_DEFEAT))ServerInteractiveUitl.admitDefeat(message);//认输
        if(message.startsWith(ClientMenu.MOVE))ServerInteractiveUitl.pieceMove(message);//走棋
        if(message.startsWith(ClientMenu.USER_EXIT))ServerInteractiveUitl.userExit(message);//断开连接
        if(message.startsWith(ClientMenu.CHAT))ServerInteractiveUitl.sendMessage(message);//聊天信息
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        System.out.println("进异常了" +ctx.channel().remoteAddress());
        ctx.close();
    }
}

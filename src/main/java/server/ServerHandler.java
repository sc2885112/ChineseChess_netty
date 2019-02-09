package server;

import enu.ClientMenu;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message){
        if(message.startsWith(ClientMenu.REQUEST_CONNECTION)) ServerInteractiveUitl.requestConnection(ctx,message);//��������
        if(message.startsWith(ClientMenu.LAUNCH_CHALLENGE))ServerInteractiveUitl.launchChallenge(message);//������ս
        if(message.startsWith(ClientMenu.ACCEPT_CHALLENGE))ServerInteractiveUitl.acceptChallenge(message);//������ս
        if(message.startsWith(ClientMenu.REFUSE_CHALLENGE))ServerInteractiveUitl.refuseChallenge(message);//�ܾ���ս
        if(message.startsWith(ClientMenu.ADMIT_DEFEAT))ServerInteractiveUitl.admitDefeat(message);//����
        if(message.startsWith(ClientMenu.MOVE))ServerInteractiveUitl.pieceMove(message);//����
        if(message.startsWith(ClientMenu.USER_EXIT))ServerInteractiveUitl.userExit(message);//�Ͽ�����
        if(message.startsWith(ClientMenu.CHAT))ServerInteractiveUitl.sendMessage(message);//������Ϣ
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        System.out.println("���쳣��" +ctx.channel().remoteAddress());
        ctx.close();
    }
}

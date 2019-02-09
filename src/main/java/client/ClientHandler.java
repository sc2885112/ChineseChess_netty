package client;

import enu.ClientMenu;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.swing.*;
import java.util.Vector;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private static ClientView clientView;

    public ClientHandler(ClientView clientView) {
        ClientHandler.clientView = clientView;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message){
        if(message.startsWith(ClientMenu.RE_USERS_LIST)) reUsersLiset(message);//收到列表刷新信息
        if(message.startsWith(ClientMenu.REPEAT_NAME)) repeatName(message);//收到重名的信息
        if(message.startsWith(ClientMenu.LAUNCH_CHALLENGE))receiveChallenge(ctx.channel(),message);//收到请求挑战信息
        if(message.startsWith(ClientMenu.ACCEPT_CHALLENGE))acceptChallenge(message);//收到接受挑战的信息
        if(message.startsWith(ClientMenu.REFUSE_CHALLENGE))refuseChallenge(message);//收到拒绝挑战的信息
        if(message.startsWith(ClientMenu.ADMIT_DEFEAT)) admitDefeat(); //收到认输的信息
        if(message.startsWith(ClientMenu.MOVE))pieceMove(message);//走棋
        if(message.startsWith(ClientMenu.OPPONENT_EXIT))opponentExit();//对手离线
        if(message.startsWith(ClientMenu.CHAT))chatMessage(message);//收到聊天信息
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        ctx.channel().close();
    }

    public void reUsersLiset(String msg){
        String s = msg.substring(12);// 分解并得到有用信息
        String[] na = s.split(",");
        Vector<String> v = new Vector<String>();
        for (int i = 0; i < na.length; i++) {
            if (na[i].trim().length() != 0 &&
                    (!na[i].trim().equals(clientView.jtfNickName.getText().trim()))) { //如果字符串部位空并且不等于本机昵称
                v.add(na[i]);
            }
        }
        clientView.jcbNickList.setModel(new DefaultComboBoxModel(v));// 设置下拉列表的值
    }

    public void repeatName(String message){
        JOptionPane.showMessageDialog(clientView, "您的昵称已被占用!");
        clientView.channel.close(); //关闭通道
        clientView.myThread.interrupt(); //停止线程
        clientView.jtfHost.setEnabled(true);// 将用于输入主机名的文本框设为可用
        clientView.jtfPort.setEnabled(true);// 将用于输入端口号的文本框设为可用
        clientView.jtfNickName.setEnabled(true);// 将用于输入昵称的文本框设为可用
        clientView.jbConnection.setEnabled(true);// 将"连接"按钮设为可用
        clientView.jbExtConnection.setEnabled(false);// 将"断开"按钮设为不可用
        clientView.jbChallenge.setEnabled(false);// 将"挑战"按钮设为不可用
        clientView.jbAdmitDefeat.setEnabled(false);// 将"认输"按钮设为不可用
    }

    public void receiveChallenge(Channel channel,String message) {
        String name = message.substring(16);
        Object[] options = { "接受", "拒绝" };
        int m = JOptionPane.showOptionDialog(clientView, name + "向您发起挑战！！", "新的挑战！", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (m == JOptionPane.YES_OPTION) {
            //初始化棋盘
            clientView.checkerboard.initPiece(true);

            //设置阵营接受挑战的为黑方
            clientView.userCamp = false;

            //设置是否已经和玩家开始对弈的标记为true
            clientView.opponentName = name;

            clientView.isCanMove = false;
            clientView.jbChallenge.setEnabled(false);// 将"挑战"按钮设为不可用
            clientView.jbAdmitDefeat.setEnabled(true);// 将"认输"按钮设为可用
            clientView.jbSendMessage.setEnabled(true);//将发送按钮可用
            // 向服务器发送接受挑战的信息
            channel.writeAndFlush(ClientMenu.ACCEPT_CHALLENGE + clientView.myNickName + "-" + name);

        } else {
            // 向服务发送拒绝挑战的信息
            channel.writeAndFlush(ClientMenu.REFUSE_CHALLENGE + clientView.myNickName + "-" + name);
        }
    }

    public void acceptChallenge(String message){
        String name = message.substring(16);
        //初始化棋盘
        clientView.checkerboard.initPiece(false);
        //设置阵营发起挑战的为红方
        clientView.userCamp = true;
        //设置对手
        clientView.opponentName = name;

        clientView.isCanMove = true;

        JOptionPane.showMessageDialog(clientView, name+"接受的您的挑战！！\n开始对战吧!", "接受挑战！！",JOptionPane.PLAIN_MESSAGE);
        clientView.jbChallenge.setEnabled(false);// 将"挑战"按钮设为不可用
        clientView.jbAdmitDefeat.setEnabled(true);// 将"认输"按钮设为可用
        clientView.jbSendMessage.setEnabled(true);//将发送按钮可用
    }

    public void refuseChallenge(String message){
        String name = message.substring(16);
        JOptionPane.showMessageDialog(clientView, name + "拒绝了您的挑战!!" , "拒绝挑战", JOptionPane.PLAIN_MESSAGE);
    }

    public void admitDefeat(){
        JOptionPane.showMessageDialog(clientView, "您的对手认输了！" , "胜利", JOptionPane.PLAIN_MESSAGE);
        if(clientView.userCamp){
            clientView.checkerboard.initPiece(false);//初始化棋盘
        }else{
            clientView.checkerboard.initPiece(true);//初始化棋盘
        }
        clientView.opponentName = null; //将对手置空
        clientView.isCanMove = true;//初始化走棋标记
        clientView.jbChallenge.setEnabled(true);// 将"挑战"按钮设可用
        clientView.jbAdmitDefeat.setEnabled(false);// 将"认输"按钮设为不可用
        clientView.jbSendMessage.setEnabled(false);//将发送按钮不可用
        clientView.textArea.setText(null);//清空聊天框
    }

    public void pieceMove(String message){
        String[] msg = message.split(",");

        int startX =  8 - Integer.parseInt(msg[1]);
        int startY = 9 - Integer.parseInt(msg[2]);
        int endX = 8 - Integer.parseInt(msg[3]);
        int endY = 9 - Integer.parseInt(msg[4]);

        Piece target = clientView.checkerboard.pieceList[endX][endY];

        clientView.pieceMove(clientView.checkerboard.pieceList[startX][startY], endX, endY); //走棋

        clientView.isCanMove = true; //修改可以移动的标记
        clientView.checkerboard.repaint();

        if (target != null)// 判断是否分出胜负
            clientView.isWinning(target, endX, endY);
    }

    public void opponentExit(){
        JOptionPane.showMessageDialog(clientView, "您的对手退出了,您赢了！" , "胜利", JOptionPane.PLAIN_MESSAGE);
        if(clientView.userCamp){
            clientView.checkerboard.initPiece(false);//初始化棋盘
        }else{
            clientView.checkerboard.initPiece(true);//初始化棋盘
        }
        clientView.opponentName = null; //将对手置空
        clientView.isCanMove = true;//初始化走棋标记
        clientView.jbChallenge.setEnabled(true);// 将"挑战"按钮设可用
        clientView.jbAdmitDefeat.setEnabled(false);// 将"认输"按钮设为不可用
        clientView.jbSendMessage.setEnabled(false);//将发送按钮不可用
        clientView.textArea.setText(null);//清空聊天框
    }

    public void chatMessage(String message){
        String name = message.substring(message.lastIndexOf("-") + 1 , message.length());
        message = message.substring(5,message.lastIndexOf("-"));
        clientView.textArea.append(name + "对你说:\n    " + message + "\n\n");
    }

}

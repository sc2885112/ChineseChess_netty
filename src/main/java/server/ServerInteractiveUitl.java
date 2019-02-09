package server;

import enu.ClientMenu;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class ServerInteractiveUitl {

    private static ConcurrentHashMap<String, ChannelHandlerContext> onlineList=new ConcurrentHashMap<>();//当前在线用户的线程列表
    private static ServerView serverView;


    private ServerInteractiveUitl(){}

    public static void setServerView(ServerView serverView) {
        ServerInteractiveUitl.serverView = serverView;
    }

    public static void requestConnection(ChannelHandlerContext channel ,String message){
        String name = message.substring(18);// 截取用户昵称
        if (onlineList.get(name) != null) {// 如果重名
            channel.writeAndFlush(ClientMenu.REPEAT_NAME);
        }else {
            onlineList.put(name,channel);// 将该通道添加到在线列表
            reUsersList();// 刷新服务器在线信息列表

            String nickListMsg = "";

            Enumeration<String> keys = onlineList.keys();
            while(keys.hasMoreElements()){
                nickListMsg = nickListMsg + "," + keys.nextElement();// 将在线列表内容住组织成字符串
            }

            nickListMsg = ClientMenu.RE_USERS_LIST + nickListMsg;

            keys = onlineList.keys();
            ChannelHandlerContext satTemp;
            while(keys.hasMoreElements()){
                satTemp = onlineList.get(keys.nextElement());
                satTemp.writeAndFlush(nickListMsg);// 将最新的列表信息发送到各个客户端
            }
        }
    }

    public static void launchChallenge(String message) {
        String[] names = message.split("-");// 截取用户昵称

        ChannelHandlerContext channel = onlineList.get(names[2]);
        if(channel != null){//如果不为空的话发送挑战信息
            channel.writeAndFlush(ClientMenu.LAUNCH_CHALLENGE + names[1]);
        }else{//如果为空的话回发离线信息
            channel = onlineList.get(names[1]);
            channel.writeAndFlush(ClientMenu.USER_EXIT + names[2]);
        }
    }


    public static void acceptChallenge(String message){
        String []names = message.split("-");

        ChannelHandlerContext channel = onlineList.get(names[2]);
        //向客户端发送接受挑战的信息
        if(channel != null){
            channel.writeAndFlush(ClientMenu.ACCEPT_CHALLENGE + names[1]);
        }else{//如果为空的话回发离线信息
            channel = onlineList.get(names[1]);
            channel.writeAndFlush(ClientMenu.USER_EXIT + names[2]);
        }
    }


    public static void refuseChallenge(String message){
        String []names = message.split("-");
        ChannelHandlerContext channel = onlineList.get(names[2]);
        if(channel != null){
            channel.writeAndFlush(ClientMenu.REFUSE_CHALLENGE + names[1]);
        }else{//如果为空的话回发离线信息
            channel = onlineList.get(names[1]);
            channel.writeAndFlush(ClientMenu.USER_EXIT + names[2]);
        }
    }

    public static void admitDefeat(String message){
        String name = message.substring(12);
        ChannelHandlerContext channel = onlineList.get(name);
        channel.writeAndFlush(ClientMenu.ADMIT_DEFEAT);
    }

    public static void pieceMove(String message){
        String name = message.substring(5).split(",")[0];
        ChannelHandlerContext channel = onlineList.get(name);
        channel.writeAndFlush(ClientMenu.MOVE + message);
    }

    public static void userExit(String message){
        String [] names = message.split("-");
        if(names.length >= 3){//如果name不为空的话向对手发送离线消息
            onlineList.get(names[2]).writeAndFlush(ClientMenu.OPPONENT_EXIT);
        }

        onlineList.remove(names[1]);
        reUsersList();

        String nickListMsg = "";
        Enumeration<String> keys = onlineList.keys();
        while(keys.hasMoreElements()){
            nickListMsg = nickListMsg + "," + keys.nextElement();//将在线列表内容住组织成字符串
        }

        nickListMsg = ClientMenu.RE_USERS_LIST + nickListMsg;

        keys = onlineList.keys();
        ChannelHandlerContext satTemp;
        while(keys.hasMoreElements()){
            satTemp = onlineList.get(keys.nextElement());
            satTemp.writeAndFlush(nickListMsg);// 将最新的列表信息发送到各个客户端
        }
    }

    public static void sendMessage(String message){
        String [] names = message.split("-");
        ChannelHandlerContext channel = onlineList.get(names[3]);
        channel.writeAndFlush(message.substring(0,message.lastIndexOf("-")) + "-" + names[2]);
    }

    /**
     * 更新用户在线列表
     */
    public static void reUsersList() {
        serverView.textArea.setText(null);
        ChannelHandlerContext channel;

        Enumeration<String> keys = onlineList.keys();
        while(keys.hasMoreElements()){
            String nickName = keys.nextElement();
            channel = onlineList.get(nickName);
            String msg = "";
            msg += "Address:" + channel.channel().remoteAddress() + "\n"; //拼接地址
            msg += "name:" + nickName + "\n\n"; //拼接昵称
            serverView.textArea.append(msg);
        }

    }
}

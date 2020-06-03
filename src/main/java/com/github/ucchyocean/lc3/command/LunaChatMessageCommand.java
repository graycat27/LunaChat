/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.Messages;
import com.github.ucchyocean.lc3.bukkit.LunaChatBukkit;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.member.ChannelMember;

/**
 * 1:1チャット送信コマンド
 * @author ucchy
 */
public class LunaChatMessageCommand implements CommandExecutor {

    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {

        // senderからChannelMemberを作成する
        ChannelMember inviter = ChannelMember.getChannelMember(sender);

        // 引数が無ければ、usageを表示して終了する
        if (args.length == 0) {
            printUsage(sender, label);
            return true;
        }

        // 実行引数から1:1チャットの相手を取得する
        String invitedName = "";
        StringBuilder message = new StringBuilder();
        if (args.length >= 1) {
            invitedName = args[0];
            if ( args.length >= 2 ) {
                for ( int i=1; i<args.length; i++ ) {
                    message.append(args[i] + " ");
                }
            }
        } else {
            sender.sendMessage(Messages.errmsgCommand());
            printUsage(sender, label);
            return true;
        }

        // メッセージを送信する
        sendTellMessage(inviter, invitedName, message.toString().trim());
        return true;
    }

    /**
     * Tellコマンドの実行処理を行う
     * @param inviter
     * @param invitedName
     * @param message
     */
    protected void sendTellMessage(ChannelMember inviter, String invitedName, String message) {

        // 招待相手が存在するかどうかを確認する
        ChannelMember invited = ChannelMember.getChannelMember(invitedName);
        if ( invited == null || !invited.isOnline() ) {
            inviter.sendMessage(Messages.errmsgNotfoundPlayer(invitedName));
            return;
        }

        // 招待相手が自分自身でないか確認する
        if (inviter.getName().equals(invited.getName())) {
            inviter.sendMessage(Messages.errmsgCannotSendPMSelf());
            return;
        }

        // チャンネルが存在するかどうかをチェックする
        LunaChatAPI api = LunaChatBukkit.getInstance().getLunaChatAPI();
        String cname = inviter.getName() + ">" + invited.getName();
        Channel channel = api.getChannel(cname);
        if ( channel == null ) {
            // チャンネルを作成して、送信者、受信者をメンバーにする
            channel = api.createChannel(cname);
            channel.setVisible(false);
            channel.addMember(inviter);
            channel.addMember(invited);
            channel.setPrivateMessageTo(invited.getName());
        }

        // メッセージがあるなら送信する
        if ( message.trim().length() > 0 ) {
            channel.chat(inviter, message);
        }

        // 送信履歴を残す
        DataMaps.privateMessageMap.put(
                invited.getName(), inviter.getName());
        DataMaps.privateMessageMap.put(
                inviter.getName(), invited.getName());

        return;
    }

    /**
     * コマンドの使い方を senderに送る
     * @param sender
     * @param label
     */
    private void printUsage(CommandSender sender, String label) {
        sender.sendMessage(Messages.usageMessage(label));
    }
}

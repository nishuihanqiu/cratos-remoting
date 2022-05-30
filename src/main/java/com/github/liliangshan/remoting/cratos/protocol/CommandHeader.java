package com.github.liliangshan.remoting.cratos.protocol;

/**
 * CommandHeader .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class CommandHeader {

    private long requestId;

    private int commandType;

    private int bodyLength;

    public static CommandHeader of(long requestId, int commandType, int bodyLength) {
        CommandHeader commandHeader = new CommandHeader();
        commandHeader.setRequestId(requestId);
        commandHeader.setCommandType(commandType);
        commandHeader.setBodyLength(bodyLength);
        return commandHeader;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

}

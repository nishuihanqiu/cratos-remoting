package com.github.liliangshan.remoting.cratos.protocol;

/**
 * Command .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class Command {

    public static final short MAGIC = (short) 0xF0F0;

    private CommandHeader header;
    private byte[] body;

    public static Command of(byte[] body, long requestId, CommandType commandType) {
        Command command = new Command();
        int bodyLength = body == null ? 0 : body.length;
        command.setHeader(CommandHeader.of(requestId, commandType.getCode(), bodyLength));
        command.setBody(body);
        return command;
    }

    public void setHeader(CommandHeader header) {
        this.header = header;
    }

    public CommandHeader getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public short getMagic() {
        return MAGIC;
    }

}

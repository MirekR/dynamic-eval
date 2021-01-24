package cz.mirek.dynamic.javascript.dto;

public class CustomFunction {
    private String topic;
    private String javascript;

    public String getTopic() {
        return topic;
    }

    public CustomFunction setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getJavascript() {
        return javascript;
    }

    public CustomFunction setJavascript(String javascript) {
        this.javascript = javascript;
        return this;
    }
}

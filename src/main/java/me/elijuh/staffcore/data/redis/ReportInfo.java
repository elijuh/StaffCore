package me.elijuh.staffcore.data.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportInfo {
    private final String server;
    private final String reporter;
    private final String reported;
    private final String reason;
}

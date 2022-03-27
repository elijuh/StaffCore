package me.elijuh.staffcore.data.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageInfo {
    private final String permission;
    private final String message;
}

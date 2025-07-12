package cn.fpsboost.util.lang;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author LangYa466
 * @date 2025/5/18
 */
@Getter
@RequiredArgsConstructor
public enum Langs {
    CN("中文"),
    EN("English");

    private final String displayName;
}

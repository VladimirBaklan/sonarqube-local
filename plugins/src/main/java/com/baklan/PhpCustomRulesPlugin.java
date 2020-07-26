package com.baklan;

import org.sonar.api.Plugin;

public class PhpCustomRulesPlugin implements Plugin {
    @Override
    public void define(Context context) {
        context.addExtension(PhpRules.class);
    }
}

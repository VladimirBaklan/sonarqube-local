package com.baklan;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;

public class PhpRules implements RulesDefinition, PHPCustomRuleRepository {

    @Override
    public String repositoryKey() {
        return "custom";
    }

    @Override
    public ImmutableList<Class> checkClasses() {
        return ImmutableList.of(UselessPublicMethods.class);
    }

    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(repositoryKey(), "php").setName("MyCompany Custom Repository");

        RulesDefinitionAnnotationLoader annotationLoader = new RulesDefinitionAnnotationLoader();
        checkClasses().forEach(ruleClass -> annotationLoader.load(repository, ruleClass));

        repository.rules().forEach(rule -> rule.setHtmlDescription(loadResource("/org/sonar/l10n/php/rules/custom/" + rule.key() + ".html")));

        Map<String, String> remediationCosts = new HashMap<>();
        remediationCosts.put(UselessPublicMethods.KEY, "5min");
        repository.rules().forEach(rule -> rule.setDebtRemediationFunction(
                rule.debtRemediationFunctions().constantPerIssue(remediationCosts.get(rule.key()))));

        repository.done();
    }

    private String loadResource(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Resource not found: " + path);
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (InputStream in = resource.openStream()) {
            byte[] buffer = new byte[1024];
            for (int len = in.read(buffer); len != -1; len = in.read(buffer)) {
                result.write(buffer, 0, len);
            }
            return new String(result.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }
}

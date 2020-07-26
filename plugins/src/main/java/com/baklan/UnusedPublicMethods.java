package com.baklan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
@Rule(
        key = UnusedPublicMethods.KEY,
        priority = Priority.MAJOR,
        name = "Unused method.",
        tags = {"unused", "custom"}
)
public class UnusedPublicMethods extends PHPVisitorCheck {
    public static final String KEY = "345834905834095834905";
    private static final String MESSAGE = "Remove this unused \"%s\" method.";

    private List<String> stringLiterals = new ArrayList<>();

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
        stringLiterals.clear();
        super.visitClassDeclaration(tree);

        if (tree.is(Tree.Kind.CLASS_DECLARATION)) {
            checkClass(tree);
        }
    }

    private void checkClass(ClassTree tree) {
        Scope classScope = context().symbolTable().getScopeFor(tree);
        for (Symbol methodSymbol : classScope.getSymbols(Kind.FUNCTION)) {

            boolean ruleConditions = methodSymbol.usages().isEmpty();

            if (ruleConditions
                    && !isConstructor(methodSymbol.declaration(), tree)
                    && !isMagicMethod(methodSymbol.name())
                    && !isUsedInStringLiteral(methodSymbol)) {
                context().newIssue(this, methodSymbol.declaration(), String.format(MESSAGE, methodSymbol.name()));
            }
        }
    }

    @Override
    public void visitAnonymousClass(AnonymousClassTree tree) {
        stringLiterals.clear();
        super.visitAnonymousClass(tree);

        checkClass(tree);
    }

    @Override
    public void visitLiteral(LiteralTree tree) {
        if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
            stringLiterals.add(CheckUtils.trimQuotes(tree).toLowerCase(Locale.ROOT));
        }
    }


    private boolean isUsedInStringLiteral(Symbol methodSymbol) {
        for (String stringLiteral : stringLiterals) {
            if (stringLiteral.contains(methodSymbol.name())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isConstructor(IdentifierTree methodName, ClassTree classDec) {
        MethodDeclarationTree constructor = classDec.fetchConstructor();
        return  constructor != null && constructor.name().equals(methodName);
    }

    private static boolean isMagicMethod(String methodName) {
        return methodName.startsWith("__");
    }
}

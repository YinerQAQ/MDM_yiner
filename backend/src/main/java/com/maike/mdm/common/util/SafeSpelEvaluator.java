package com.maike.mdm.common.util;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * 安全的SpEL表达式评估工具类
 * 禁止类型访问，防止RCE攻击
 */
public class SafeSpelEvaluator {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    // 禁止的关键字列表
    private static final String[] BLOCKED_KEYWORDS = {
        "T(", "Runtime", "ProcessBuilder", "Class.forName",
        "getClass()", "java.lang.Runtime", "java.io", "java.net",
        "exec(", "System.exit", "Thread.", "ClassLoader"
    };

    /**
     * 评估SpEL表达式，返回Boolean结果
     * @param expression SpEL表达式
     * @param variables 变量Map
     * @return 表达式的Boolean结果
     */
    public static Boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        validateExpression(expression);
        StandardEvaluationContext context = createSafeContext(variables);
        return PARSER.parseExpression(expression).getValue(context, Boolean.class);
    }

    /**
     * 评估SpEL表达式，返回Object结果
     * @param expression SpEL表达式
     * @param variables 变量Map
     * @return 表达式的执行结果
     */
    public static Object evaluate(String expression, Map<String, Object> variables) {
        validateExpression(expression);
        StandardEvaluationContext context = createSafeContext(variables);
        return PARSER.parseExpression(expression).getValue(context);
    }

    /**
     * 创建安全的EvaluationContext，禁止类型访问
     */
    private static StandardEvaluationContext createSafeContext(Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 禁止类型访问 - 这是防止RCE的关键
        context.setTypeLocator(typeName -> {
            throw new SecurityException("Type access is not allowed in expressions: " + typeName);
        });
        // 设置变量
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        return context;
    }

    /**
     * 校验表达式是否包含禁止的关键字
     */
    private static void validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }
        String upper = expression.toUpperCase();
        for (String keyword : BLOCKED_KEYWORDS) {
            if (upper.contains(keyword.toUpperCase())) {
                throw new SecurityException("Expression contains blocked keyword: " + keyword);
            }
        }
    }
}

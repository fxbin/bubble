package cn.fxbin.bubble.fireworks.plugin.lock.aop.support;

import cn.fxbin.bubble.fireworks.core.util.StringUtils;
import cn.fxbin.bubble.fireworks.plugin.lock.annotation.LockAction;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LockKeyGenerator
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/14 16:58
 */
public class LockKeyGenerator {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();


    /**
     * Generate a key for the given package and method.
     * @param method a single method on a class or interface
     * @param lockAction the annotation of {@see com.zichan360.framework.lock.annotation.Lock}
     * @return a generated key
     */
    public Object generate(Method method, LockAction lockAction) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName());
        sb.append(".").append(method.getName());

        if(lockAction.keys().length > 0 && !lockAction.keys()[0].isEmpty()) {
            List<String> keys = new ArrayList<>(Arrays.asList(lockAction.keys()));
            String keysStr = StringUtils.collectionToDelimitedString(keys, ".", "", "");
            sb.append(".").append(keysStr);
        }
        return sb.toString();
    }


    /**
     * generate, 解析Spring EL 表达式
     *
     * @since 2020/8/4 15:28
     * @param expression 表达式
     * @param method 方法
     * @param args 方法参数
     * @return java.lang.String
     */
    public String generate(String expression, Method method, Object [] args) {
        String[] parameterNames = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < (parameterNames != null ? parameterNames.length : 0); i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(expression).getValue(context, String.class);
    }

}

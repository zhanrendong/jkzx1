package tech.tongyu.bct.workflow.process.config.condition;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import tech.tongyu.bct.workflow.process.config.FormData;

public class Condition<T> {

    private Index<T> index;
    private Expression expression;

    static class Temp<T> {
        T indexValue;
        Temp(T indexValue){
            this.indexValue = indexValue;
        }
    }

    public Condition(Index<T> index, String expressionString){
        this.index = index;
        this.expression = new SpelExpressionParser().parseExpression(expressionString);
    }

    public Boolean isTrue(FormData formData){
        return expression.getValue(new Temp<>(this.index.calculate(formData)), Boolean.class);
    }
}

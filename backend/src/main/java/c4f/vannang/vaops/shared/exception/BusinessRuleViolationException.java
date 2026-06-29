package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class BusinessRuleViolationException extends AbstractPlatformException {


    public BusinessRuleViolationException() {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, "Business rule violated");
    }
    public BusinessRuleViolationException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message, cause);
    }

    public BusinessRuleViolationException(String message, Map<String, Object> details) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message, details);
    }
}

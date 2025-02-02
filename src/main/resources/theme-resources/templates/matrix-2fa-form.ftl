<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp'); section>
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form id="kc-form-login" action="${url.loginAction}" method="post">
                    <div class="alert alert-info">
                        <p>${msg("matrix-2fa.otpSent", matrixUserId)}</p>
                    </div>

                    <div class="form-group">
                        <label for="otp" class="control-label">
                            ${msg("matrix-2fa.enterCode")}
                        </label>
                        <input tabindex="1"
                               id="otp"
                               class="form-control"
                               name="otp"
                               type="text"
                               inputmode="numeric"
                               pattern="[0-9]*"
                               minlength="${otpLength}"
                               maxlength="${otpLength}"
                               autocomplete="one-time-code"
                               autofocus
                               required />
                        
                        <#if messagesPerField.existsError('otp')>
                            <span id="input-error-otp" class="error-text" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('otp'))?no_esc}
                            </span>
                        </#if>
                    </div>

                    <div class="form-group">
                        <div id="kc-form-buttons" class="submit">
                            <input tabindex="2"
                                   class="button button-primary button-large"
                                   name="login"
                                   id="kc-login"
                                   type="submit"
                                   value="${msg("doSubmit")}" />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>
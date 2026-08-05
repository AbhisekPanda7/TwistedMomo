package com.twistedmomos.backend.notification;

/**
 * Verification email markup.
 *
 * <p>Tables and inline styles rather than modern CSS: Outlook renders with Word's engine,
 * and Gmail strips {@code <style>} blocks, so flexbox, grid and classes are unavailable.
 * Colours mirror the site's brand tokens — ink-950, gold-400, paper-50.
 */
public final class VerificationEmailTemplate {

    private static final String INK = "#07070a";
    private static final String INK_CARD = "#131318";
    private static final String GOLD = "#f5b700";
    private static final String PAPER = "#faf4e3";
    private static final String MUTED = "#8a8a93";

    private VerificationEmailTemplate() {}

    public static String render(String name, String link) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Confirm your email</title>
            </head>
            <body style="margin:0;padding:0;background-color:%1$s;">
              <!-- Shown in the inbox list under the subject, so it should not repeat it. -->
              <div style="display:none;max-height:0;overflow:hidden;opacity:0;">
                One tap and your Twisted Momos account is ready.
              </div>

              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
                     style="background-color:%1$s;padding:32px 16px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
                           style="max-width:520px;background-color:%2$s;border-radius:16px;overflow:hidden;">

                      <tr>
                        <td align="center" style="padding:44px 32px 12px 32px;">
                          <div style="font-family:Georgia,'Times New Roman',serif;font-size:26px;
                                      font-weight:700;letter-spacing:1px;color:%4$s;">
                            TWISTED <span style="color:%3$s;">MOMOS</span>
                          </div>
                        </td>
                      </tr>

                      <tr>
                        <td align="center" style="padding:20px 32px 0 32px;">
                          <h1 style="margin:0;font-family:Helvetica,Arial,sans-serif;font-size:24px;
                                     line-height:1.25;font-weight:700;color:%4$s;">
                            Confirm your email
                          </h1>
                        </td>
                      </tr>

                      <tr>
                        <td align="center" style="padding:16px 40px 0 40px;">
                          <p style="margin:0;font-family:Helvetica,Arial,sans-serif;font-size:15px;
                                    line-height:1.6;color:%5$s;">
                            Hi %6$s — tap below to confirm your address and finish setting up your account.
                          </p>
                        </td>
                      </tr>

                      <tr>
                        <td align="center" style="padding:32px 32px 0 32px;">
                          <!-- Bulletproof button: a padded anchor, since Outlook ignores
                               button elements and background images. -->
                          <a href="%7$s"
                             style="display:inline-block;padding:14px 32px;border-radius:999px;
                                    background-color:%3$s;color:%1$s;text-decoration:none;
                                    font-family:Helvetica,Arial,sans-serif;font-size:15px;
                                    font-weight:700;letter-spacing:0.5px;">
                            Confirm my email
                          </a>
                        </td>
                      </tr>

                      <tr>
                        <td align="center" style="padding:26px 40px 0 40px;">
                          <p style="margin:0;font-family:Helvetica,Arial,sans-serif;font-size:12px;
                                    line-height:1.6;color:%5$s;">
                            The link works once and expires in 24 hours.
                          </p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:32px 32px 0 32px;">
                          <div style="height:1px;background-color:#2a2a32;line-height:1px;">&nbsp;</div>
                        </td>
                      </tr>

                      <tr>
                        <td align="center" style="padding:20px 40px 40px 40px;">
                          <p style="margin:0 0 8px 0;font-family:Helvetica,Arial,sans-serif;
                                    font-size:11px;line-height:1.6;color:%5$s;">
                            Button not working? Paste this into your browser:
                          </p>
                          <!-- word-break stops long tokens overflowing narrow mobile clients. -->
                          <p style="margin:0;font-family:Helvetica,Arial,sans-serif;font-size:11px;
                                    line-height:1.5;color:%3$s;word-break:break-all;">
                            %7$s
                          </p>
                        </td>
                      </tr>
                    </table>

                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
                           style="max-width:520px;">
                      <tr>
                        <td align="center" style="padding:20px 32px;">
                          <p style="margin:0;font-family:Helvetica,Arial,sans-serif;font-size:11px;
                                    line-height:1.6;color:%5$s;">
                            Didn't sign up? Ignore this email and nothing happens.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """
                .formatted(INK, INK_CARD, GOLD, PAPER, MUTED, escape(name), escape(link));
    }

    /** The name is user-supplied and lands in markup — escape it rather than trust it. */
    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

=begin
= 実行方法

== Java2 SE 1.4

 $ java \
         -jar dist/pandaclient.jar \
         -host=localhost \
         -user=sample \
         -pass=sample \
         panda:helloworld

== Java2 SE 1.3

 $ XMLLIBS=/usr/share/java/xercesImpl.jar:/usr/share/java/xmlParserAPIs.jar
 $ java \
         -cp dist/pandaclient.jar:$XMLLIBS \
         -host=localhost \
         -user=sample \
         -pass=sample \
         panda:helloworld

= プロパティ

: monsia.logger.factory
  ロギングに使用するクラスを指定。設定できるのは以下の4通り。
  : org.montsuqi.util.NullLogger
    一切ログを出力しない。
  : org.montsuqi.util.StdErrLogger
    標準エラー出力に出力。
  : org.montsuqi.util.Log4JLogger
    ApacheのLog4Jを使用。
  : org.montsuqi.util.J2SELogger
    Java2 SDK 1.4のロギングAPIを使用。
: monsia.document.handler
  画面定義ファイルのパーズに使用するクラスを指定。
  : org.montsuqi.monsia.Glade1Handler
    Glade version 1形式の定義ファイルを使用。
  : org.montsuqi.monsia.MonsiaHandler
    Monsia形式の定義ファイルを使用。
  決定手順は以下のとおり。
  (1) monsia.document.handlerの値
  (2) monsia.document.handlerが未定義なら、定義ファイルの冒頭40バイト程度を先読みして、GTK-Interfaceという字句を探す。
  (3) GTK-Interfaceの字句がなければ新形式。
  (4) ここまでで決定できなければ旧形式。
: swing.defaultlaf
  ルック&フィールを指定。
  : com.sun.java.swing.plaf.windows.WindowsLookAndFeel
    Windows風(Java2 SDK 1.4ではXP風)
  : com.sun.java.swing.plaf.motif.MotifLookAndFeel
    Motif風
  などが設定可能。
=end

=begin
=MacOS X環境での動作について

松江から拝借しているMatintoshで現状のクライアントを動作させてみました。

== 環境

このMacintoshのソフトウェア構成は

*MacOS X 10.1.5
*Java2 SDK 1.3.1

となっているのですが、Java2にXML処理ライブラリが標準で附属するようになっ
たのはJava2 SDK 1.4以降です。このため、

(1)SDKのバージョンを、1.4以降にアップデートする
(2)別途配布のライブラリを入手する

のいずれかを行う必要があります。

まず1.案を採ろうとAppleのサイトにて調べたところ、AppleのJava SDK 1.4実
装を使用するにはMacOS自体を10.2.xにアップデートする必要があるとのこと。
しかし、無償のOSアップデート手段が見付からなかったので、これは断念し、
2.案を採ることにしました。

==手順
(1)((<Java XML Pack|URL:http://java.sun.com/xml/downloads/javaxmlpack.html>))を入手
(2)展開して得られたjarアーカイブをSDKの拡張ライブラリフォルダに置く
(3)Terminalを起動
(4)コマンドラインから、
    java -jar dist/pandaclient.jar -host=foo -user=sample -pass=sample panda:helloworld
   のようにJava版クライアントを実行。

以上の手順で、Windows/Linux上と同程度に動作することが確認できました。

==TODO
Terminalを使わない起動方法の調査。
==メモ
*((<Java Runtime Properties for MacOS X|URL:http://developer.apple.com/ja/technotes/tn2031.html>))
*((<Tailoring Java Applications for MacOS X|URL:http://developer.apple.com/ja/technotes/tn2042.html>))
=end

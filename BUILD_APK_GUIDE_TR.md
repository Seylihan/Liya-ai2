# Liya AI APK oluşturma rehberi

Bu paket Android kaynak kodudur; APK dosyası değildir. Bu çalışma ortamında Android SDK ve Gradle kurulu olmadığı için APK burada derlenemedi.

## GitHub Actions ile APK oluşturma
1. GitHub'da yeni, boş bir repository oluştur.
2. Bu ZIP'i bilgisayarında aç ve içindeki dosyaları repository'ye yükle. `.github/workflows/android-build.yml` dosyasının da yüklendiğinden emin ol.
3. Repository'de **Actions** sekmesine gir.
4. **Build Liya Android APK** iş akışını seç.
5. **Run workflow** düğmesine bas.
6. İşlem tamamlanınca ilgili çalıştırmayı aç. **Artifacts** bölümünden `LiyaAI-debug-apk` dosyasını indir.
7. ZIP içindeki `app-debug.apk` dosyasını Android telefonuna aktar ve kur.

## Liya'nın ilk açılışı
- Ayarlar sekmesinde OpenAI uyumlu bir Chat Completions API sağlayıcısının URL'sini, model adını ve API anahtarını gir.
- Sohbette `Liya, öğren: ...` yazarak hafızaya bilgi kaydedebilirsin.
- Mikrofon kullanımı ilk seferde ses izni ister.

## Dürüst teknik durum
- APK henüz oluşturulmadı veya cihazda test edilmedi.
- Bu sürüm internet üzerinden AI API'sine bağlanır ve sohbet/hafıza verilerini cihazda Room veritabanında tutar.
- Gerçek cihaz içi yerel dil modeli henüz ekli değildir. Bu sürümde “hybrid”, online model + telefondaki yerel hafıza anlamındadır.
- API anahtarını doğrudan mobil uygulamada saklamak üretim için ideal değildir. Uygulama paylaşılacaksa anahtarı güvenli sunucu tarafında tutan bir proxy önerilir.

# Modular Player — Forge 1.20.1 starter (v0.2)

**Mục tiêu: thay thế toàn diện model player theo module, thay cơ thể và trang phục độc lập. Chiều cao không phải trọng tâm.**

Repository: https://github.com/susu609/ModularPlayer  
Mod ID: `modularplayer` · Java 17 · Minecraft 1.20.1 · Forge 47.4.10

## Phạm vi bản alpha

Đây là bản thử nghiệm kiến trúc và renderer cho local player (third person/inventory), **chưa phải hệ thống avatar hoàn thiện**. Chưa xác nhận `gradle compileJava` trong môi trường dựng nguồn.

- Skin Steve nội bộ làm nền; mesh hình học của mod được render thay cho body vanilla của người chơi cục bộ.
- Có preset `Shape.STEVE` và `Shape.WIDE`, chọn riêng cho **đầu / thân / tay / chân**.
- **Mũ / áo / quần / giày** là các render pass với texture PNG độc lập, mỗi slot có `NONE`; chưa tích hợp inventory equipment.
- Animation vanilla vẫn cung cấp pose cho các phần cơ thể. Mesh được mod điều khiển với `ModelPart.copyFrom(...)`.
- Chỉ thay đổi hình ảnh local player; **không sửa hitbox, camera, eye height hoặc server gameplay**.
- Chưa hỗ trợ đầy đủ tay góc nhìn thứ nhất, armor, elytra, cape, skin người chơi khác, đồng bộ multiplayer, GUI editor, import Blockbench hoặc accessory 3D.
- Phím tắt thay đổi ngoại hình chỉ lưu trong phiên chạy hiện tại.

## Nơi sửa code

| Muốn sửa | File |
|---|---|
| Tạo preset mesh mới, hình học từng bộ phận | `src/main/java/com/susu609/modularplayer/client/model/BodyGeometry.java` |
| Điều khiển render cơ thể và quần áo | `src/main/java/com/susu609/modularplayer/client/model/ModularPlayerModel.java` |
| Thêm loại shape hoặc slot outfit | `src/main/java/com/susu609/modularplayer/appearance/Appearance.java` |
| Thay trạng thái ngoại hình khi chơi | `src/main/java/com/susu609/modularplayer/client/AppearanceState.java` |
| Đăng ký và bắt phím đổi đồ | `src/main/java/com/susu609/modularplayer/client/EditorHotkeys.java` |
| Default các bộ phận | `src/main/java/com/susu609/modularplayer/ClientConfig.java` |
| Thay player renderer | `src/main/java/com/susu609/modularplayer/client/mixin/PlayerRendererMixin.java` |
| Đăng ký model layers | `src/main/java/com/susu609/modularplayer/client/ClientRendererRegistration.java` |
| Texture cơ thể Steve | `src/main/resources/assets/modularplayer/textures/body/steve.png` |
| Texture từng món đồ | `src/main/resources/assets/modularplayer/textures/outfit/*.png` |

## Phím thử trong game

- `B`: đổi mesh **thân** Steve/Wide
- `V`: đổi **áo** (không áo / Steve xanh / Steve đỏ)
- `N`: đổi **quần** (không quần / Steve pants)
- `M`: đổi **giày** (không giày / Steve boots)

Đổi shape đầu/tay/chân và mũ trong `config/modularplayer-client.toml` (hoặc `run/config/modularplayer-client.toml`). Ví dụ file cấu hình sinh sau lần khởi động đầu tiên:

```toml
[appearance]
  enabled = true
  [appearance.body]
    head = "STEVE"
    torso = "WIDE"
    arms = "STEVE"
    legs = "STEVE"
  [appearance.outfit]
    headwear = "NONE"
    top = "STEVE_BLUE"
    bottom = "STEVE_PANTS"
    feet = "NONE"
```

## Hướng phát triển

1. **BodyPart Registry + JSON/Blockbench geometry loader**: dùng `ResourceLocation` thay cho các shape hardcode.
2. **Clothing Registry / equipment**: model hình khối riêng cho áo quần, chất liệu, độ dày, layer ordering và xử lý clipping.
3. **Avatar Editor**: GUI preview 3D, chọn từng bộ phận, lưu profile, import/export.
4. **Gameplay integration**: first-person hands, armor, elytra, animation, multiplayer sync và hitbox tùy chọn.
5. **API mở** để mod khác thêm body part, garment và preset skin.

## Build & Test

Yêu cầu JDK 17, Gradle 8.1.1 và Internet để tải Forge. Chưa đóng gói binary Gradle wrapper. Dùng Gradle đã cài:

```bash
gradle compileJava
gradle runClient
```

Workflow `.github/workflows/compile.yml` chạy compile trên GitHub Actions. Bộ test Python `tests/test_starter_assets.py` kiểm tra texture 64×64, resource JSON và đường dẫn trong code; **không thay thế kiểm thử Forge runtime**.

### Texture

Texture Steve và lớp trang phục demo là pixel art tự dựng theo UV Steve 64×64, không dùng trực tiếp skin thương mại. Outfit atlas tách riêng cho từng slot để có thể thay áo, quần, mũ và giày mà không cần thay skin cơ thể.

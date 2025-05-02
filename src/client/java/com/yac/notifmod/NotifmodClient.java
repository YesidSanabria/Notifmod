package com.yac.notifmod;

import com.yac.notifmod.networking.payload.OakCallPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper; // Correct import

@Environment(EnvType.CLIENT)
public class NotifmodClient implements ClientModInitializer {

	// --- Estado ---
	private static boolean showOakCall = false;
	private static long oakCallStartTime = 0L;
	private static final Identifier OAK_TEXTURE = Identifier.of("notifmod", "textures/gui/oak.png");

	// --- Constantes de Configuración (Ajustadas para el nuevo layout) ---
	private static final int UI_WIDTH = 200; // Ancho total
	private static final int TOP_SECTION_HEIGHT = 20; // Altura sección superior (azul oscuro)
	private static final int BOTTOM_SECTION_HEIGHT = 40; // Altura sección inferior (azul claro)
	private static final int UI_HEIGHT = TOP_SECTION_HEIGHT + BOTTOM_SECTION_HEIGHT; // Altura total = 60

	private static final int OAK_IMAGE_SIZE = 32; // Tamaño imagen Oak (ajustado para caber en la sección inferior)
	private static final int PADDING = 4; // Espaciado interno

	private static final int TEXT_COLOR_TITLE = 0xFFFFFFFF; // Blanco
	private static final int TEXT_COLOR_MESSAGE = 0xFFFFFFFF; // Blanco (o un gris claro si prefieres)

	// Colores estimados de tu imagen (con transparencia ARGB - ajusta si es necesario)
	// 90% opacidad para el oscuro, 80% para el claro
	private static final int COLOR_TOP = 0xE60B4F87; // Azul oscuro semi-transparente (~90% A)
	private static final int COLOR_BOTTOM = 0xCC41A6BF; // Azul claro semi-transparente (~80% A)

	private static final long SLIDE_DURATION_MS = 600;
	private static final long VISIBLE_DURATION_MS = 5000;
	private static final long TOTAL_DURATION_MS = SLIDE_DURATION_MS + VISIBLE_DURATION_MS + SLIDE_DURATION_MS;

	private static final Text TITLE = Text.literal("Profesor Oak");
	private static final Text MESSAGE = Text.literal("¡Quiere hablar contigo!"); // Mensaje actualizado

	@Override
	public void onInitializeClient() {
		registerPacketHandlers();
		registerHudRenderer();
	}

	private void registerPacketHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(
				OakCallPayload.ID,
				(payload, context) -> {
					context.client().execute(() -> {
						showOakCall = true;
						oakCallStartTime = System.currentTimeMillis();
					});
				}
		);
	}

	private void registerHudRenderer() {
		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			if (!showOakCall) {
				return;
			}

			MinecraftClient client = MinecraftClient.getInstance();
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - oakCallStartTime;

			if (elapsedTime > TOTAL_DURATION_MS) {
				showOakCall = false;
				return;
			}

			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight(); // Necesitamos la altura
			float progress;
			float currentX;

			// --- Cálculo X (Animación Horizontal - Sin cambios) ---
			if (elapsedTime < SLIDE_DURATION_MS) {
				progress = (float)elapsedTime / SLIDE_DURATION_MS;
				currentX = MathHelper.lerp(progress, screenWidth, screenWidth - UI_WIDTH);
			} else if (elapsedTime < SLIDE_DURATION_MS + VISIBLE_DURATION_MS) {
				currentX = screenWidth - UI_WIDTH;
			} else {
				progress = (float)(elapsedTime - SLIDE_DURATION_MS - VISIBLE_DURATION_MS) / SLIDE_DURATION_MS;
				currentX = MathHelper.lerp(progress, screenWidth - UI_WIDTH, screenWidth);
			}

			// --- Cálculo Y (Posición Vertical Centrada) ---
			int currentY = (screenHeight - UI_HEIGHT) / 2; // Centrado vertical

			// --- Dibujo de la Interfaz ---
			renderOakCallUI(drawContext, (int)currentX, currentY);
		});
	}

	// --- MÉTODO ACTUALIZADO para dibujar el nuevo layout ---
	private void renderOakCallUI(DrawContext drawContext, int x, int y) {
		MinecraftClient client = MinecraftClient.getInstance();
		int textHeight = client.textRenderer.fontHeight;

		// 1. Dibujar Fondo Superior (Azul Oscuro)
		drawContext.fill(x, y, x + UI_WIDTH, y + TOP_SECTION_HEIGHT, COLOR_TOP);

		// 2. Dibujar Fondo Inferior (Azul Claro)
		int bottomY = y + TOP_SECTION_HEIGHT;
		drawContext.fill(x, bottomY, x + UI_WIDTH, bottomY + BOTTOM_SECTION_HEIGHT, COLOR_BOTTOM);

		// (Opcional: Dibujar el borde inset. Se puede hacer dibujando líneas o rectángulos más pequeños)
		// Por simplicidad, lo omitimos por ahora.

		// 3. Dibujar Título (Centrado verticalmente en la sección superior)
		int titleX = x + PADDING;
		int titleY = y + (TOP_SECTION_HEIGHT - textHeight) / 2;
		drawContext.drawText(client.textRenderer, TITLE, titleX, titleY, TEXT_COLOR_TITLE, true); // Añadido sombra

		// 4. Dibujar Imagen de Oak (Sección inferior, derecha, centrada verticalmente)
		int imageX = x + UI_WIDTH - OAK_IMAGE_SIZE - PADDING;
		int imageY = bottomY + (BOTTOM_SECTION_HEIGHT - OAK_IMAGE_SIZE) / 2;
		// Asegúrate que el Identifier OAK_TEXTURE esté bien definido arriba
		// Los últimos 4 parámetros de drawTexture definen el tamaño de la fuente y destino en la textura.
		// Si tu oak.png es exactamente OAK_IMAGE_SIZE x OAK_IMAGE_SIZE, puedes usar 0, 0, OAK_IMAGE_SIZE, OAK_IMAGE_SIZE.
		// Si es más grande, ajusta los valores u/v y tamaño de región.
		int textureSize = 256; // Asume que tu archivo oak.png es 256x256, ajusta si es necesario
		drawContext.drawTexture(OAK_TEXTURE, imageX, imageY, OAK_IMAGE_SIZE, OAK_IMAGE_SIZE, 0, 0, textureSize, textureSize, textureSize, textureSize);


		// 5. Dibujar Mensaje (Sección inferior, izquierda de la imagen)
		int messageX = x + PADDING;
		// Centrado vertical simple en la sección inferior (puedes ajustar si quieres más espacio arriba/abajo)
		int messageY = bottomY + (BOTTOM_SECTION_HEIGHT - textHeight) / 2;
		int availableTextWidth = UI_WIDTH - OAK_IMAGE_SIZE - PADDING * 3; // Ancho para el texto

		// Podrías necesitar TextRenderer.wrapLines si el mensaje es largo, pero este es corto.
		drawContext.drawText(client.textRenderer, MESSAGE, messageX, messageY, TEXT_COLOR_MESSAGE, true); // Añadido sombra
	}
}
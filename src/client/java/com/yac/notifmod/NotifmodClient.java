package com.yac.notifmod;

import com.yac.notifmod.Notifmod;
import com.yac.notifmod.networking.payload.OakCallPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer; // Import TextRenderer
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
// import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.OrderedText; // Import para texto envuelto
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List; // Import para List

@Environment(EnvType.CLIENT)
public class NotifmodClient implements ClientModInitializer {

	// --- Estado ---
	private static boolean isCallRinging = false;
	private static boolean showSlidingCallUI = false;
	private static long slidingUICallStartTime = 0L;
	// --- Variables para tamaño dinámico ---
	private static int currentUiWidth = 0;
	private static int currentUiHeight = 0;
	private static int currentTextAreaWidth = 0;
	private static int currentBottomSectionHeight = 0;
	private static List<OrderedText> currentWrappedMessage = null;
	// --- Variable para guardar el nombre recibido ---
	private static String currentTargetPlayerName = null;

	// --- Recursos ---
	private static final Identifier CALL_PROMPT_TEXTURE = Identifier.of(Notifmod.MOD_ID, "textures/gui/call_prompt.png");
	private static final Identifier OAK_TEXTURE_SLIDING = Identifier.of(Notifmod.MOD_ID, "textures/gui/oak.png");

	// --- Constantes del Prompt (Ajustadas por ti) ---
	private static final int PROMPT_WIDTH = 50;
	private static final int PROMPT_HEIGHT = 50;
	private static final int PROMPT_PADDING = 10;

	// --- Constantes de la UI Deslizante ---
	private static final int SLIDING_UI_TOP_SECTION_HEIGHT = 20;
	private static final int SLIDING_UI_OAK_IMAGE_SIZE = 32;
	private static final int SLIDING_UI_PADDING = 5;
	private static final int SLIDING_UI_TEXT_IMAGE_PADDING = 4;
	private static final int SLIDING_UI_MAX_TEXT_WIDTH = 250; // Ancho MÁXIMO para el texto

	// --- Colores (Ajustados por ti) ---
	private static final int SLIDING_UI_TEXT_COLOR_TITLE = 0xFFFFFFFF;
	private static final int SLIDING_UI_TEXT_COLOR_MESSAGE = 0xFFFFFFFF;
	private static final int SLIDING_UI_COLOR_TOP = 0xE699002f; // Tu color oscuro
	private static final int SLIDING_UI_COLOR_BOTTOM = 0x80e0e0e0; // Tu color claro

	// --- Tiempos Animación ---
	private static final long SLIDING_UI_ANIM_DURATION_MS = 600;
	private static final long SLIDING_UI_VISIBLE_DURATION_MS = 8500;

	// --- Textos (Título fijo, Mensaje se elimina porque es dinámico) ---
	private static final Text SLIDING_UI_TITLE = Text.literal("Profesor Oak");
	// --- ELIMINADO: Mensaje estático ---
	// private static final Text SLIDING_UI_MESSAGE = Text.literal("...");

	// --- Key Binding ---
	private static KeyBinding answerCallKeyBinding;

	@Override
	public void onInitializeClient() {
		registerKeyBindings();
		registerPacketHandlers();
		registerHudRenderer();
		registerTickEvents();
	}

	private void registerKeyBindings() {
		answerCallKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.notifmod.answer_call", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.category.notifmod"));
	}

	// --- Packet Handler (Guarda el nombre recibido) ---
	private void registerPacketHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(
				OakCallPayload.ID,
				(payload, context) -> {
					context.client().execute(() -> {
						if (!isCallRinging && !showSlidingCallUI) {
							// Guarda el nombre del payload
							currentTargetPlayerName = payload.playerName();

							isCallRinging = true;
							Notifmod.LOGGER.info("DEBUG: Ejecutando código para reproducir sonido para {}", currentTargetPlayerName);
							Notifmod.LOGGER.info("Intentando reproducir sonido...");
							context.client().getSoundManager().play(
									PositionedSoundInstance.master(Notifmod.OAK_CALL_RINGTONE_EVENT, 1.0F, 1.0F)
							);
							Notifmod.LOGGER.info("¡Llamada de Oak recibida para {}! Presiona '{}' para contestar.", currentTargetPlayerName, KeyBindingHelper.getBoundKeyOf(answerCallKeyBinding).getTranslationKey());
						}
					});
				}
		);
	}

	// --- HUD Renderer (Limpia el nombre al terminar) ---
	private void registerHudRenderer() {
		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();

			// Dibuja el Prompt
			if (isCallRinging) {
				// ... (código del prompt sin cambios) ...
				int screenWidth = client.getWindow().getScaledWidth();
				int screenHeight = client.getWindow().getScaledHeight();
				int x = screenWidth - PROMPT_WIDTH - PROMPT_PADDING;
				int y = screenHeight - PROMPT_HEIGHT - PROMPT_PADDING;
				drawContext.drawTexture(CALL_PROMPT_TEXTURE, x, y, PROMPT_WIDTH, PROMPT_HEIGHT, 0, 0, PROMPT_WIDTH, PROMPT_HEIGHT, PROMPT_WIDTH, PROMPT_HEIGHT);
			}

			// Dibuja la UI Deslizante
			if (showSlidingCallUI) {
				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - slidingUICallStartTime;
				long totalDuration = SLIDING_UI_ANIM_DURATION_MS + SLIDING_UI_VISIBLE_DURATION_MS + SLIDING_UI_ANIM_DURATION_MS;

				if (elapsedTime > totalDuration) {
					showSlidingCallUI = false;
					currentWrappedMessage = null;
					currentTargetPlayerName = null; // Limpia el nombre guardado
					return;
				}

				// ... (Cálculo de animación X e Y sin cambios, usa currentUiWidth/Height) ...
				int screenWidth = client.getWindow().getScaledWidth();
				int screenHeight = client.getWindow().getScaledHeight();
				float progress;
				float currentX;
				if (elapsedTime < SLIDING_UI_ANIM_DURATION_MS) {
					progress = (float)elapsedTime / SLIDING_UI_ANIM_DURATION_MS;
					currentX = MathHelper.lerp(progress, screenWidth, screenWidth - currentUiWidth);
				} else if (elapsedTime < SLIDING_UI_ANIM_DURATION_MS + SLIDING_UI_VISIBLE_DURATION_MS) {
					currentX = screenWidth - currentUiWidth;
				} else {
					progress = (float)(elapsedTime - SLIDING_UI_ANIM_DURATION_MS - SLIDING_UI_VISIBLE_DURATION_MS) / SLIDING_UI_ANIM_DURATION_MS;
					currentX = MathHelper.lerp(progress, screenWidth - currentUiWidth, screenWidth);
				}
				int currentY = (screenHeight - currentUiHeight) / 2;


				renderSlidingCallUI(drawContext, (int)currentX, currentY);
			}
		});
	}

	// --- Tick Handler (Construye mensaje dinámico) ---
	private void registerTickEvents() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client == null || client.textRenderer == null) return;

			if (isCallRinging && answerCallKeyBinding.wasPressed()) {
				isCallRinging = false;

				// --- Construye Mensaje Dinámico y Calcula Dimensiones ---
				TextRenderer textRenderer = client.textRenderer;
				int textHeight = textRenderer.fontHeight;

				// Usa el nombre guardado (con valor por defecto por seguridad)
				String playerName = (currentTargetPlayerName != null) ? currentTargetPlayerName : "Entrenador";
				Text messageText = Text.literal("Hola " + playerName + ", Ven a verme de inmediato, tengo algo importante que decirte.");

				// Calcula dimensiones basado en el mensaje dinámico
				int titleWidth = textRenderer.getWidth(SLIDING_UI_TITLE);
				currentWrappedMessage = textRenderer.wrapLines(messageText, SLIDING_UI_MAX_TEXT_WIDTH);
				int messageHeight = currentWrappedMessage.isEmpty() ? 0 : currentWrappedMessage.size() * textHeight;
				int maxMessageLineWidth = 0;
				if (!currentWrappedMessage.isEmpty()){
					for (OrderedText line : currentWrappedMessage) {
						maxMessageLineWidth = Math.max(maxMessageLineWidth, textRenderer.getWidth(line));
					}
				}
				currentTextAreaWidth = Math.max(titleWidth, maxMessageLineWidth);
				currentUiWidth = SLIDING_UI_PADDING + currentTextAreaWidth + SLIDING_UI_TEXT_IMAGE_PADDING + SLIDING_UI_OAK_IMAGE_SIZE + SLIDING_UI_PADDING;
				currentBottomSectionHeight = Math.max(messageHeight, SLIDING_UI_OAK_IMAGE_SIZE) + SLIDING_UI_PADDING * 2;
				currentUiHeight = SLIDING_UI_TOP_SECTION_HEIGHT + currentBottomSectionHeight;

				// Activa UI y Timer
				showSlidingCallUI = true;
				slidingUICallStartTime = System.currentTimeMillis();
				Notifmod.LOGGER.info("Llamada de Oak contestada, mostrando UI para {} ({}x{})", playerName, currentUiWidth, currentUiHeight);

				while (answerCallKeyBinding.wasPressed()) { }
			}
		});
	}

	// renderSlidingCallUI (Sin cambios necesarios aquí)
	private void renderSlidingCallUI(DrawContext drawContext, int x, int y) {
		// ... (Este método no necesita cambios, ya usa currentWrappedMessage) ...
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.textRenderer == null) return;
		TextRenderer textRenderer = client.textRenderer;
		int textHeight = textRenderer.fontHeight;

		int topSectionY = y;
		int bottomSectionY = y + SLIDING_UI_TOP_SECTION_HEIGHT;

		drawContext.fill(x, topSectionY, x + currentUiWidth, bottomSectionY, SLIDING_UI_COLOR_TOP);
		drawContext.fill(x, bottomSectionY, x + currentUiWidth, bottomSectionY + currentBottomSectionHeight, SLIDING_UI_COLOR_BOTTOM);

		int titleX = x + SLIDING_UI_PADDING;
		int titleY = topSectionY + (SLIDING_UI_TOP_SECTION_HEIGHT - textHeight) / 2;
		drawContext.drawText(textRenderer, SLIDING_UI_TITLE, titleX, titleY, SLIDING_UI_TEXT_COLOR_TITLE, true);

		int imageX = x + currentUiWidth - SLIDING_UI_OAK_IMAGE_SIZE - SLIDING_UI_PADDING;
		int imageY = bottomSectionY + (currentBottomSectionHeight - SLIDING_UI_OAK_IMAGE_SIZE) / 2;
		int textureSize = 256; // ¡¡¡AJUSTA ESTO al tamaño real de tu archivo oak.png!!!
		drawContext.drawTexture(OAK_TEXTURE_SLIDING, imageX, imageY, SLIDING_UI_OAK_IMAGE_SIZE, SLIDING_UI_OAK_IMAGE_SIZE, 0, 0, textureSize, textureSize, textureSize, textureSize);

		if (currentWrappedMessage != null && !currentWrappedMessage.isEmpty()) {
			int messageX = x + SLIDING_UI_PADDING;
			int totalMessageBlockHeight = currentWrappedMessage.size() * textHeight;
			int messageStartY = bottomSectionY + (currentBottomSectionHeight - totalMessageBlockHeight) / 2;

			for (int i = 0; i < currentWrappedMessage.size(); i++) {
				OrderedText line = currentWrappedMessage.get(i);
				int lineY = messageStartY + i * textHeight;
				if (line != null) {
					drawContext.drawText(textRenderer, line, messageX, lineY, SLIDING_UI_TEXT_COLOR_MESSAGE, true);
				}
			}
		}
	}
}
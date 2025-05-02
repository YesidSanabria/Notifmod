package com.yac.notifmod.client.toast;

import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.gui.DrawContext;      // ¡Importante en 1.19.4+!
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items; // Usaremos un item existente como icono por ahora
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
// import net.minecraft.client.render.GameRenderer; // Descomentar si usas texturas personalizadas

public class OakCallToast implements Toast {

    // --- DECLARACIONES DE CAMPOS (ASEGÚRATE QUE ESTÉN) ---
    private long startTime;
    private boolean justUpdated;
    private final Text title;
    private final Text description;

    // Referencia explícita a la textura de fondo por si Toast.TEXTURE sigue fallando
    // Normalmente no es necesario, pero como sigue fallando, lo añadimos por seguridad.
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("minecraft", "toast/background");

    public OakCallToast() {
        // Inicializa los campos final en el constructor
        this.title = Text.literal("§eLlamada Entrante"); // §e es código de color amarillo
        this.description = Text.literal("Profesor Oak");
        // Los campos no-final (startTime, justUpdated) se inicializan a 0/false por defecto
    }

    @Override
    public Toast.Visibility draw(DrawContext drawContext, ToastManager manager, long startTime) {
        // Accede a los campos usando 'this.' (aunque no siempre es estrictamente necesario si no hay ambigüedad)
        if (this.justUpdated) {
            this.startTime = startTime; // Asigna al campo 'startTime' de la instancia
            this.justUpdated = false;
        }

        // Usa la referencia explícita a la textura
        drawContext.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());

        // Accede a los campos 'title' y 'description'
        drawContext.drawText(manager.getClient().textRenderer, this.title, 30, 7, 0xFFFFFF00, false);
        drawContext.drawText(manager.getClient().textRenderer, this.description, 30, 18, 0xFFFFFFFF, false);

        ItemStack icon = new ItemStack(Items.CLOCK);
        drawContext.drawItem(icon, 8, 8);

        // Usa el campo 'startTime' de la instancia
        return (startTime - this.startTime) < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    @Override
    public Object getType() {
        return this;
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public int getRequiredSpaceCount() {
        return 1;
    }

    // Método para actualizar el campo 'justUpdated'
    public void setJustUpdated() {
        this.justUpdated = true;
    }
}

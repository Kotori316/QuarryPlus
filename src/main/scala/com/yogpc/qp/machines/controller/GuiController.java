package com.yogpc.qp.machines.controller;

/*
@OnlyIn(Dist.CLIENT)
public class GuiController extends Screen implements IHandleButton {
    private GuiSlotEntities slot;
    private TextFieldWidget search;
    List<ResourceLocation> names;
    @javax.annotation.Nonnull
    private final List<ResourceLocation> allEntities;
    ResourceLocation dim;
    final int xc, yc, zc;

    public GuiController(final ResourceLocation d, final int x, final int y, final int z, final List<ResourceLocation> allEntities) {
        super(new TranslationTextComponent(Holder.blockController().getTranslationKey()));
        this.dim = d;
        this.xc = x;
        this.yc = y;
        this.zc = z;
        this.allEntities = allEntities;
        names = allEntities.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
    }

    @Override
    public void init() {
        super.init();
        this.slot = new GuiSlotEntities(this.minecraft, this.width, this.height, 30, this.height - 60, 18, this);
        this.children.add(slot);
        addButton(new IHandleButton.Button(-1, this.width / 2 - 125, this.height - 26, 250, 20, I18n.format(TranslationKeys.DONE), this));
        setFocused(slot);
        this.search = new TextFieldWidget(font, this.width / 2 - 125, this.height - 56, 250, 20, "");
        this.children.add(search);
        search.setCanLoseFocus(true);
        search.setResponder(this::searchEntities);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.render(mouseX, mouseY, partialTicks);
            this.search.render(mouseX, mouseY, partialTicks);
        }
        super.render(mouseX, mouseY, partialTicks);
        drawCenteredString(this.font, I18n.format(TranslationKeys.YOG_SPAWNER_SETTING), this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getMinecraft().player.isAlive())
            this.getMinecraft().player.closeScreen();
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!search.isFocused() && keyCode == this.getMinecraft().gameSettings.keyBindInventory.getKey().getKeyCode())) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void actionPerformed(IHandleButton.Button button) {
        if (button.id == -1) {
            GuiSlotEntities.Entry selected = slot.func_230958_g_();
            if (selected != null) {
                PacketHandler.sendToServer(SetEntity.create(dim, new BlockPos(xc, yc, zc), selected.location));
            }
            getMinecraft().player.closeScreen();
        }
    }

    public void buildModList(Consumer<GuiSlotEntities.Entry> modListViewConsumer, Function<ResourceLocation, GuiSlotEntities.Entry> newEntry) {
        names.stream().map(newEntry).forEach(modListViewConsumer);
    }

    public void searchEntities(String text) {
        List<ResourceLocation> collect;
        if (!text.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(text);
                collect = allEntities.stream().filter(l -> pattern.matcher(l.toString()).find())
                    .sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
            } catch (PatternSyntaxException e) {
                collect = allEntities.stream().filter(l -> l.toString().contains(text))
                    .sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
            }
        } else {
            collect = allEntities.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
        }
        names = collect;
        this.slot.refreshList();
        this.slot.func_241215_a_(null); // setSelected
    }
}
*/
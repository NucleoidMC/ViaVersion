/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifier;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Attributes1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Enchantments1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.MapDecorations1_20_3;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;

final class StructuredDataConverter {

    private static final Map<StructuredDataKey<?>, DataConverter<?>> REWRITERS = new Reference2ObjectOpenHashMap<>();

    static {
        register(StructuredDataKey.DAMAGE, (data, tag) -> tag.putInt("Damage", data));
        register(StructuredDataKey.UNBREAKABLE, (data, tag) -> {
            tag.putBoolean("Unbreakable", true);
            if (!data.showInTooltip()) {
                putHideFlag(tag, 0x04);
            }
        });
        register(StructuredDataKey.CUSTOM_NAME, (data, tag) -> tag.putString("CustomName", ComponentUtil.tagToJsonString(data)));
        register(StructuredDataKey.LORE, (data, tag) -> {
            final ListTag<StringTag> lore = new ListTag<>(StringTag.class);
            for (final Tag loreEntry : data) {
                lore.add(new StringTag(ComponentUtil.tagToJsonString(loreEntry)));
            }
            tag.put("Lore", lore);
        });
        register(StructuredDataKey.ENCHANTMENTS, (data, tag) -> convertEnchantments(data, tag, false));
        register(StructuredDataKey.STORED_ENCHANTMENTS, (data, tag) -> convertEnchantments(data, tag, true));
        register(StructuredDataKey.ATTRIBUTE_MODIFIERS, (data, tag) -> {
            final ListTag<CompoundTag> modifiers = new ListTag<>(CompoundTag.class);
            for (final AttributeModifier modifier : data.modifiers()) {
                final String identifier = Attributes1_20_3.idToKey(modifier.attribute());
                if (identifier == null) {
                    continue;
                }

                final CompoundTag modifierTag = new CompoundTag();
                modifierTag.putString("AttributeName", identifier);
                modifierTag.putString("Name", modifier.modifier().name());
                modifierTag.putDouble("Amount", modifier.modifier().amount());
                modifierTag.putInt("Slot", modifier.slot());
                modifierTag.putInt("Operation", modifier.modifier().operation());
                modifiers.add(modifierTag);
            }
            tag.put("AttributeModifiers", modifiers);

            if (!data.showInTooltip()) {
                putHideFlag(tag, 0x02);
            }
        });
        register(StructuredDataKey.CUSTOM_MODEL_DATA, (data, tag) -> tag.putInt("CustomModelData", data));
        register(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP, (data, tag) -> putHideFlag(tag, 0x20));
        register(StructuredDataKey.REPAIR_COST, (data, tag) -> tag.putInt("RepairCost", data));
        register(StructuredDataKey.DYED_COLOR, (data, tag) -> {
            tag.putInt("color", data.rgb());
            if (!data.showInTooltip()) {
                putHideFlag(tag, 0x40);
            }
        });
        register(StructuredDataKey.MAP_COLOR, (data, tag) -> tag.putInt("MapColor", data));
        register(StructuredDataKey.MAP_ID, (data, tag) -> tag.putInt("map", data));
        register(StructuredDataKey.MAP_DECORATIONS, (data, tag) -> {
            final ListTag<CompoundTag> decorations = new ListTag<>(CompoundTag.class);
            for (final Map.Entry<String, Tag> entry : data.entrySet()) {
                final CompoundTag decorationTag = (CompoundTag) entry.getValue();
                final int id = MapDecorations1_20_3.keyToId(decorationTag.getString("type"));
                if (id == -1) {
                    continue;
                }

                final CompoundTag convertedDecoration = new CompoundTag();
                convertedDecoration.putString("id", entry.getKey());
                convertedDecoration.putInt("type", id);
                convertedDecoration.putDouble("x", decorationTag.getDouble("x"));
                convertedDecoration.putDouble("z", decorationTag.getDouble("z"));
                convertedDecoration.putFloat("rot", decorationTag.getFloat("rotation"));
                decorations.add(convertedDecoration);
            }
            tag.put("Decorations", decorations);
        });
        register(StructuredDataKey.WRITABLE_BOOK_CONTENT, (data, tag) -> {
            final ListTag<StringTag> pages = new ListTag<>(StringTag.class);
            final CompoundTag filteredPages = new CompoundTag();
            for (int i = 0; i < data.length; i++) {
                final FilterableString page = data[i];
                pages.add(new StringTag(page.raw()));
                if (page.filtered() != null) {
                    filteredPages.putString(Integer.toString(i), page.filtered());
                }
            }
            tag.put("pages", pages);
            tag.put("filtered_pages", filteredPages);
        });
        register(StructuredDataKey.WRITTEN_BOOK_CONTENT, (data, tag) -> {
            final ListTag<StringTag> pages = new ListTag<>(StringTag.class);
            final CompoundTag filteredPages = new CompoundTag();
            for (int i = 0; i < data.pages().length; i++) {
                final FilterableComponent page = data.pages()[i];
                pages.add(new StringTag(ComponentUtil.tagToJsonString(page.raw())));
                if (page.filtered() != null) {
                    filteredPages.putString(Integer.toString(i), ComponentUtil.tagToJsonString(page.filtered()));
                }
            }
            tag.put("pages", pages);
            tag.put("filtered_pages", filteredPages);

            tag.putString("author", data.author());
            tag.putInt("generation", data.generation());
            tag.putBoolean("resolved", data.resolved());
            tag.putString("title", data.title().raw());
            if (data.title().filtered() != null) {
                tag.putString("filtered_title", data.title().filtered());
            }
        });
        register(StructuredDataKey.BASE_COLOR, (data, tag) -> tag.putInt("Base", data));
        register(StructuredDataKey.CHARGED_PROJECTILES, (data, tag) -> convertItemList(data, tag, "ChargedProjectiles"));
        register(StructuredDataKey.BUNDLE_CONTENTS, (data, tag) -> convertItemList(data, tag, "Items"));
        register(StructuredDataKey.LODESTONE_TRACKER, (data, tag) -> {
            final CompoundTag positionTag = new CompoundTag();
            tag.put("LodestonePos", positionTag);
            tag.putBoolean("LodestoneTracked", data.tracked());
            tag.putString("LodestoneDimension", data.pos().dimension());
            positionTag.putInt("X", data.pos().x());
            positionTag.putInt("Y", data.pos().y());
            positionTag.putInt("Z", data.pos().z());
        });
        register(StructuredDataKey.FIREWORKS, (data, tag) -> {
            final CompoundTag fireworksTag = new CompoundTag();
            fireworksTag.putInt("Flight", data.flightDuration());
            tag.put("Fireworks", fireworksTag);

            final ListTag<CompoundTag> explosionsTag = new ListTag<>(CompoundTag.class);
            for (final FireworkExplosion explosion : data.explosions()) {
                explosionsTag.add(convertExplosion(explosion));
            }
            fireworksTag.put("Explosions", explosionsTag);
        });
        register(StructuredDataKey.FIREWORK_EXPLOSION, (data, tag) -> tag.put("Explosion", convertExplosion(data)));
        register(StructuredDataKey.PROFILE, (data, tag) -> {
            if (data.name() != null && data.id() == null && data.properties().length == 0) {
                tag.putString("SkullOwner", data.name());
                return;
            }

            final CompoundTag profileTag = new CompoundTag();
            tag.put("SkullOwner", profileTag);
            if (data.name() != null) {
                profileTag.putString("Name", data.name());
            }
            if (data.id() != null) {
                profileTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(data.id())));
            }

            final CompoundTag propertiesTag = new CompoundTag();
            for (final GameProfile.Property property : data.properties()) {
                final ListTag<CompoundTag> values = new ListTag<>(CompoundTag.class);
                final CompoundTag propertyTag = new CompoundTag();
                propertyTag.putString("Value", property.value());
                if (property.signature() != null) {
                    propertyTag.putString("Signature", property.signature());
                }
                values.add(propertyTag);
                propertiesTag.put(property.name(), values);
            }
        });
        register(StructuredDataKey.INSTRUMENT, (data, tag) -> {
            if (!data.hasId()) {
                // Can't do anything with direct values
                return;
            }

            final String identifier = Instruments1_20_3.idToKey(data.id());
            if (identifier != null) {
                tag.putString("instrument", identifier);
            }
        });
        //register(StructuredDataKey., (data, tag) -> );

        //TODO
        // StructuredDataKey.CAN_PLACE_ON
        // StructuredDataKey.CAN_BREAK
        // StructuredDataKey<PotionContents> POTION_CONTENT
        // StructuredDataKey<SuspiciousStewEffect[]> SUSPICIOUS_STEW_EFFECT
        // StructuredDataKey<ArmorTrim> TRIM
        // StructuredDataKey<CompoundTag> DEBUG_STICK_STATE
        // StructuredDataKey<CompoundTag> ENTITY_DATA
        // StructuredDataKey<CompoundTag> BUCKET_ENTITY_DATA
        // StructuredDataKey<CompoundTag> BLOCK_ENTITY_DATA
        // StructuredDataKey<Tag> RECIPES
        // StructuredDataKey<BannerPatternLayer[]> BANNER_PATTERNS
        // StructuredDataKey<BlockStateProperties> BLOCK_STATE
        // StructuredDataKey<int[]> POT_DECORATIONS
        // StructuredDataKey<String> NOTE_BLOCK_SOUND
        // StructuredDataKey<Bee[]> BEES
        // StructuredDataKey<Tag> LOCK
        // StructuredDataKey<CompoundTag> CONTAINER_LOOT
        // StructuredDataKey<Item[]> CONTAINER
        // StructuredDataKey<Unit> CREATIVE_SLOT_LOCK
        // StructuredDataKey<Boolean> ENCHANTMENT_GLINT_OVERRIDE
        // StructuredDataKey<Unit> INTANGIBLE_PROJECTILE
        // StructuredDataKey<Integer> MAP_POST_PROCESSING
    }

    private static CompoundTag convertExplosion(final FireworkExplosion explosion) {
        final CompoundTag explosionTag = new CompoundTag();
        explosionTag.putInt("Type", explosion.shape());
        explosionTag.put("Colors", new IntArrayTag(explosion.colors().clone()));
        explosionTag.put("FadeColors", new IntArrayTag(explosion.fadeColors().clone()));
        explosionTag.putBoolean("Trail", explosion.hasTrail());
        explosionTag.putBoolean("Flicker", explosion.hasTwinkle());
        return explosionTag;
    }

    private static void convertItemList(final Item[] items, final CompoundTag tag, final String key) {
        final ListTag<CompoundTag> itemsTag = new ListTag<>(CompoundTag.class);
        for (final Item item : items) {
            final CompoundTag savedItem = new CompoundTag();
            savedItem.putString("id", "stone"); // TODO
            savedItem.putByte("Count", (byte) item.amount());

            final CompoundTag itemTag = new CompoundTag();
            for (final StructuredData<?> data : item.structuredData().data().values()) {
                writeToTag(data, itemTag);
            }
            savedItem.put("tag", itemTag);
            itemsTag.add(savedItem);
        }
        tag.put(key, itemsTag);
    }

    private static void convertEnchantments(final Enchantments data, final CompoundTag tag, final boolean storedEnchantments) {
        final ListTag<CompoundTag> enchantments = new ListTag<>(CompoundTag.class);
        for (final Int2IntMap.Entry entry : data.enchantments().int2IntEntrySet()) {
            final String identifier = Enchantments1_20_3.idToKey(entry.getIntKey());
            if (identifier == null) {
                continue;
            }

            final CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", identifier);
            enchantment.putShort("lvl", (short) entry.getIntKey());
            enchantments.add(enchantment);
        }
        tag.put(storedEnchantments ? "StoredEnchantments" : "Enchantments", enchantments);

        if (!data.showInTooltip()) {
            putHideFlag(tag, storedEnchantments ? 0x20 : 0x01);
        }
    }

    private static void putHideFlag(final CompoundTag tag, final int value) {
        tag.putInt("HideFlags", tag.getInt("HideFlags") | value);
    }

    public static <T> void writeToTag(final StructuredData<T> data, final CompoundTag tag) {
        if (data.isEmpty()) {
            return;
        }

        //noinspection unchecked
        final DataConverter<T> converter = (DataConverter<T>) REWRITERS.get(data.key());
        if (converter != null) {
            converter.convert(data.value(), tag);
        }
    }

    private static <T> void register(final StructuredDataKey<T> key, final DataConverter<T> converter) {
        REWRITERS.put(key, converter);
    }

    @FunctionalInterface
    interface DataConverter<T> {

        void convert(T data, CompoundTag tag);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.downloader.emx.serializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.molgenis.downloader.api.EntitySerializer;
import org.molgenis.downloader.api.metadata.Attribute;
import org.molgenis.downloader.api.metadata.Entity;
import org.molgenis.downloader.api.metadata.Language;
import static java.util.stream.Collectors.joining;
import org.molgenis.downloader.api.metadata.MolgenisVersion;

/**
 *
 * @author david
 */
public class EMXAttributeSerializer implements EntitySerializer<Attribute> {

    private static final String[] FIELDS = {
        "entity", "name", "label", "dataType", "refEntity", "nillable",
        "idAttribute", "enumOptions", "defaultValue", "rangeMin", "rangeMax",
        "lookupAttribute", "labelAttribute", "readOnly", "aggregateable",
        "visible", "unique", "partOfAttribute", "expression",
        "validationExpression", "tags", "description",};

    public static final MolgenisVersion MIN_VERSION_FOR_MAPPEDBY = new MolgenisVersion(2, 0, 0);

    private final MolgenisVersion version;
    private final Collection<Language> languages;

    public EMXAttributeSerializer(final MolgenisVersion molgenisVersion, final Collection<Language> languages) {
        version = molgenisVersion;
        this.languages = languages;
    }

    @Override
    public List<String> serialize(final Attribute att) {
        List<String> result = new ArrayList<>();
        final Entity entity = att.getEntity();
        result.add(entity.getFullName());
        result.add(att.getName());
        result.add(att.getLabel());
        result.add(att.getDataType().name().toLowerCase());
        result.add(Optional.ofNullable(att.getRefEntity()).map(Entity::getFullName).orElse(""));
        result.add(Boolean.toString(att.isOptional()));
        final boolean isAutoIdAttriubte = att.isAuto();
        final boolean isIdAttribute = att.isIdAttribute();
        if (isIdAttribute && isAutoIdAttriubte) {
            result.add("AUTO");
        } else {
            result.add(Boolean.toString(isIdAttribute));
        }
        result.add(att.getEnumOptions());
        result.add(att.getDefaultValue());
        if (att.getDataType().isNumericType()
                && att.getRangeMin() != null && att.getRangeMax() != null
                && !att.getRangeMin().equals(att.getRangeMax())) {
            result.add(Long.toString(att.getRangeMin()));
            result.add(Long.toString(att.getRangeMax()));
        } else {
            result.add(null);
            result.add(null);
        }
        result.add(Boolean.toString(att.isLookupAttribute()));
        result.add(Boolean.toString(att.isLabelAttribute()));
        result.add(Boolean.toString(att.isReadOnly()));
        result.add(Boolean.toString(att.isAggregateable()));
        result.add(Boolean.toString(att.isVisible()));
        result.add(Boolean.toString(att.isUnique()));
        result.add(Optional.ofNullable(att.getCompound()).map(Attribute::getName).orElse(""));
        result.add(att.getExpression());
        result.add(att.getValidationExpression());
        result.add(att.getTags().stream().map(tag -> tag.getId()).collect(joining(",")));
        result.add(att.getDescription());
        if (fields().contains("mappedBy")) {
            result.add(Optional.ofNullable(att.getMappedBy()).map(by -> by.getEntity().getFullName()).orElse(""));
        }
//      Not yet supported attributes
//        result.add(att.getVisibleExpression());
//        result.add(att.getOrderBy());
        languages.forEach(language -> {
            result.add(att.getDescriptions().get(language));
            result.add(att.getLabels().get(language));
        });
        return result;
    }

    @Override
    public List<String> fields() {
        final List<String> fields = new ArrayList<>(Arrays.asList(FIELDS));
        if (version.equalsOrLargerThan(MIN_VERSION_FOR_MAPPEDBY)) {
            fields.add("mappedBy");
        }

        languages.forEach(language -> {
            fields.add("description-" + language.getCode());
            fields.add("label-" + language.getCode());
        });
        return fields;
    }

}

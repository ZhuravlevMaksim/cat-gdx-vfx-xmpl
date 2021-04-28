/*******************************************************************************
 * Copyright 2019 metaphore
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.crashinvaders.vfx.common.lml.attributes;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlBuildingAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** Generic placeholder attribute that can be used just to reserve specific attribute names in parser. */
public class DummyBuildingLmlAttribute implements LmlBuildingAttribute<LmlActorBuilder> {
    @Override
    public Class<LmlActorBuilder> getBuilderType() {
        return LmlActorBuilder.class;
    }

    @Override
    public boolean process(LmlParser parser, LmlTag tag, LmlActorBuilder builder, String rawAttributeData) {
        return true;
    }
}

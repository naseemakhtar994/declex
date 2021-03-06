/**
 * Copyright (C) 2016 DSpot Sp. z o.o
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dspot.declex.override.util;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.helper.APTCodeModelHelper;
import org.androidannotations.helper.ModelConstants;
import org.androidannotations.holder.GeneratedClassHolder;

import com.dspot.declex.util.TypeUtils;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JMod;


public class DeclexAPTCodeModelHelper extends APTCodeModelHelper {
	
	private AndroidAnnotationsEnvironment environment;
	private Map<TypeMirror, String> mappedNamesForTypeMirrors = new HashMap<TypeMirror, String>();
	
	public DeclexAPTCodeModelHelper(AndroidAnnotationsEnvironment environment) {
		super(environment);
		this.environment = environment;
	}
	
	@Override
	public TypeMirror getActualType(final Element element, DeclaredType enclosingClassType, GeneratedClassHolder holder) {
		String className = element.asType().toString();
		if (!className.contains(".") && className.endsWith(ModelConstants.generationSuffix())) {
			className = className.substring(0, className.length()-1);
			
			for (String name : TypeUtils.getAllToBeGeneratedClassesName(environment)) {
				if (name.endsWith("." + className)) {
					className = name + ModelConstants.generationSuffix();
					
					TypeElement typeElement = holder.getEnvironment().getProcessingEnvironment()
							                        .getElementUtils().getTypeElement(name);
					TypeMirror typeMirror = typeElement.asType();
					mappedNamesForTypeMirrors.put(typeMirror, className);
					return typeMirror;
				}
			}
		}
		
		return super.getActualType(element, enclosingClassType, holder);
	}
	
	@Override
	protected AbstractJClass typeMirrorToJClass(TypeMirror type,
			Map<String, TypeMirror> substitute) {
		
		if (mappedNamesForTypeMirrors.containsKey(type)) {
			AbstractJClass result = environment.getJClass(mappedNamesForTypeMirrors.get(type));
			mappedNamesForTypeMirrors.remove(type);
			return result;
		}
		
		String className = type.toString();
		if (!className.contains(".") && className.endsWith(ModelConstants.generationSuffix())) {
			className = className.substring(0, className.length()-1);
			
			for (String name : TypeUtils.getAllToBeGeneratedClassesName(environment)) {
				if (name.endsWith("." + className)) {
					className = name + ModelConstants.generationSuffix();
					return environment.getJClass(className);
				}
			}
		}
				
		return super.typeMirrorToJClass(type, substitute);
	}
	
	@Override
	public int elementVisibilityModifierToJMod(Element element) {
		return JMod.PUBLIC;
	}
}

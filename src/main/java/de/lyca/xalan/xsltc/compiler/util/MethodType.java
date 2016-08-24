/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */

package de.lyca.xalan.xsltc.compiler.util;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JType;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class MethodType extends Type {
  private final Type _resultType;
  private final List<Type> _argsType;

  public MethodType(Type resultType) {
    _argsType = null;
    _resultType = resultType;
  }

  public MethodType(Type resultType, Type arg1) {
    if (arg1 != Type.Void) {
      _argsType = new ArrayList<>();
      _argsType.add(arg1);
    } else {
      _argsType = null;
    }
    _resultType = resultType;
  }

  public MethodType(Type resultType, Type arg1, Type arg2) {
    _argsType = new ArrayList<>(2);
    _argsType.add(arg1);
    _argsType.add(arg2);
    _resultType = resultType;
  }

  public MethodType(Type resultType, Type arg1, Type arg2, Type arg3) {
    _argsType = new ArrayList<>(3);
    _argsType.add(arg1);
    _argsType.add(arg2);
    _argsType.add(arg3);
    _resultType = resultType;
  }

  public MethodType(Type resultType, List<Type> argsType) {
    _resultType = resultType;
    _argsType = argsType.size() > 0 ? argsType : null;
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder("method{");
    if (_argsType != null) {
      final int count = _argsType.size();
      for (int i = 0; i < count; i++) {
        result.append(_argsType.get(i));
        if (i != count - 1) {
          result.append(',');
        }
      }
    } else {
      result.append("void");
    }
    result.append('}');
    return result.toString();
  }

  @Override
  public JType toJCType() {
    return null; // should never be called
  }

  @Override
  public boolean identicalTo(Type other) {
    boolean result = false;
    if (other instanceof MethodType) {
      final MethodType temp = (MethodType) other;
      if (_resultType.identicalTo(temp._resultType)) {
        final int len = argsCount();
        result = len == temp.argsCount();
        for (int i = 0; i < len && result; i++) {
          final Type arg1 = _argsType.get(i);
          final Type arg2 = temp._argsType.get(i);
          result = arg1.identicalTo(arg2);
        }
      }
    }
    return result;
  }

  @Override
  public int distanceTo(Type other) {
    int result = Integer.MAX_VALUE;
    if (other instanceof MethodType) {
      final MethodType mtype = (MethodType) other;
      if (_argsType != null) {
        final int len = _argsType.size();
        if (len == mtype._argsType.size()) {
          result = 0;
          for (int i = 0; i < len; i++) {
            final Type arg1 = _argsType.get(i);
            final Type arg2 = mtype._argsType.get(i);
            final int temp = arg1.distanceTo(arg2);
            if (temp == Integer.MAX_VALUE) {
              result = temp; // return MAX_VALUE
              break;
            } else {
              result += arg1.distanceTo(arg2);
            }
          }
        }
      } else if (mtype._argsType == null) {
        result = 0; // both methods have no args
      }
    }
    return result;
  }

  public Type resultType() {
    return _resultType;
  }

  public List<Type> argsType() {
    return _argsType;
  }

  public int argsCount() {
    return _argsType == null ? 0 : _argsType.size();
  }

}

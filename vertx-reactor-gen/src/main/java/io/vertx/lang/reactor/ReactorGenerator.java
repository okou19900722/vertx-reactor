package io.vertx.lang.reactor;

import io.vertx.codegen.*;
import io.vertx.codegen.Helper;
import io.vertx.codegen.type.*;
import io.vertx.lang.rx.AbstractRxGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.vertx.codegen.type.ClassKind.*;

class ReactorGenerator extends AbstractRxGenerator {
  private static final String id = "reactor";

  ReactorGenerator() {
    super(id);
    this.kinds = Collections.singleton("class");
    this.name = "Reactor";
  }

  @Override
  protected void genRxImports(ClassModel model, PrintWriter writer) {
    writer.format("import %s;", Flux.class.getName()).println();
    writer.format("import %s;", Mono.class.getName()).println();
    super.genRxImports(model, writer);
  }

  @Override
  protected void genToObservable(ApiTypeInfo type, PrintWriter writer) {
    TypeInfo streamType = type.getReadStreamArg();
    writer.print("  private Flux<");
    writer.print(genTypeName(streamType));
    writer.println("> flux;");

    writer.println();

    genToXXXAble(streamType, "Flux", "flux", writer);
  }

  private void genToXXXAble(TypeInfo streamType, String rxType, String rxName, PrintWriter writer) {
    writer.print("  public synchronized ");
    writer.print(rxType);
    writer.print("<");
    writer.print(genTypeName(streamType));
    writer.print("> to");
    writer.print(rxType);
    writer.println("() {");

    writer.print("    ");
    writer.print("if (");
    writer.print(rxName);
    writer.println(" == null) {");

    if (streamType.getKind() == ClassKind.API) {
      writer.print("      java.util.function.Function<");
      writer.print(streamType.getName());
      writer.print(", ");
      writer.print(genTypeName(streamType));
      writer.print("> conv = ");
      writer.print(genTypeName(streamType.getRaw()));
      writer.println("::newInstance;");

      writer.print("      ");
      writer.print(rxName);
      writer.print(" = io.vertx.reactor.");
      writer.print(rxType);
      writer.print("Helper.to");
      writer.print(rxType);
      writer.println("(delegate, conv);");
    } else if (streamType.isVariable()) {
      String typeVar = streamType.getSimpleName();
      writer.print("      java.util.function.Function<");
      writer.print(typeVar);
      writer.print(", ");
      writer.print(typeVar);
      writer.print("> conv = (java.util.function.Function<");
      writer.print(typeVar);
      writer.print(", ");
      writer.print(typeVar);
      writer.println(">) __typeArg_0.wrap;");

      writer.print("      ");
      writer.print(rxName);
      writer.print(" = io.vertx.reactor.");
      writer.print(rxType);
      writer.print("Helper.to");
      writer.print(rxType);
      writer.println("(delegate, conv);");
    } else {
      writer.print("      ");
      writer.print(rxName);
      writer.print(" = io.vertx.reactor.");
      writer.print(rxType);
      writer.print("Helper.to");
      writer.print(rxType);
      writer.println("(this.getDelegate());");
    }

    writer.println("    }");
    writer.print("    return ");
    writer.print(rxName);
    writer.println(";");
    writer.println("  }");
    writer.println();
  }

//  private String genFutureMethodName(MethodInfo method) {
//    return "rx" + Character.toUpperCase(method.getName().charAt(0)) + method.getName().substring(1);
//  }

  @Override
  protected void genMethods(ClassModel model, MethodInfo method, List<String> cacheDecls, PrintWriter writer) {
    genMethod1(model, method, cacheDecls, writer);
    MethodInfo flowableOverload = genOverloadedMethod(method, Flux.class);
//    MethodInfo observableOverload = genOverloadedMethod(method, Flux.class);
    if (flowableOverload != null) {
      genMethod1(model, flowableOverload, cacheDecls, writer);
    }
//    if (observableOverload != null) {
//      genMethod(model, observableOverload, cacheDecls, writer);
//    }
  }

  @Override
  protected void genRxMethod(ClassModel model, MethodInfo method, PrintWriter writer) {
    MethodInfo futMethod = genFutureMethod(method);
    ClassTypeInfo raw = futMethod.getReturnType().getRaw();
    String methodSimpleName = raw.getSimpleName();
    String adapterType = "io.vertx.reactor.impl.AsyncResult" + methodSimpleName + ".to" + methodSimpleName;
    String rxType = raw.getName();
    startMethodTemplate(model.getType(), futMethod, "", writer);
    writer.println(" { ");
    writer.print("    return ");
    writer.print(adapterType);
    writer.println("(handler -> {");
    writer.print("      ");
    writer.print(method.getName());
    writer.print("(");
    List<ParamInfo> params = futMethod.getParams();
    writer.print(params.stream().map(ParamInfo::getName).collect(Collectors.joining(", ")));
    if (params.size() > 0) {
      writer.print(", ");
    }
    writer.println("handler);");
    writer.println("    });");
    writer.println("  }");
    writer.println();
  }

  protected void genReadStream(List<? extends TypeParamInfo> typeParams, PrintWriter writer) {
    writer.print("  Flux<");
    writer.print(typeParams.get(0).getName());
    writer.println("> toFlux();");
    writer.println();

  }

  private void genMethod1(ClassModel model, MethodInfo method, List<String> cacheDecls, PrintWriter writer) {
    genSimpleMethod(model, method, cacheDecls, writer);
    if (method.getKind() == MethodKind.FUTURE) {
      genRxMethod(model, method, writer);
    }
  }

  private void genSimpleMethod(ClassModel model, MethodInfo method, List<String> cacheDecls, PrintWriter writer) {
    ClassTypeInfo type = model.getType();
    startMethodTemplate(type, method, "", writer);
    writer.println(" { ");
    if (method.isFluent()) {
      writer.print("    ");
      writer.print(genInvokeDelegate(model, method));
      writer.println(";");
      if (method.getReturnType().isVariable()) {
        writer.print("    return (");
        writer.print(method.getReturnType().getName());
        writer.println(") this;");
      } else {
        writer.println("    return this;");
      }
    } else if (method.getReturnType().getName().equals("void")) {
      writer.print("    ");
      writer.print(genInvokeDelegate(model, method));
      writer.println(";");
    } else {
      if (method.isCacheReturn()) {
        writer.print("    if (cached_");
        writer.print(cacheDecls.size());
        writer.println(" != null) {");

        writer.print("      return cached_");
        writer.print(cacheDecls.size());
        writer.println(";");
        writer.println("    }");
      }
      String cachedType;
      TypeInfo returnType = method.getReturnType();
      if (method.getReturnType().getKind() == PRIMITIVE) {
        cachedType = ((PrimitiveTypeInfo) returnType).getBoxed().getName();
      } else {
        cachedType = genTypeName(returnType);
      }
      writer.print("    ");
      writer.print(genTypeName(returnType));
      writer.print(" ret = ");
      writer.print(genConvReturn(returnType, method, genInvokeDelegate(model, method)));
      writer.println(";");
      if (method.isCacheReturn()) {
        writer.print("    cached_");
        writer.print(cacheDecls.size());
        writer.println(" = ret;");
        cacheDecls.add("private" + (method.isStaticMethod() ? " static" : "") + " " + cachedType + " cached_" + cacheDecls.size());
      }
      writer.println("    return ret;");
    }
    writer.println("  }");
    writer.println();
  }

  private String genInvokeDelegate(ClassModel model, MethodInfo method) {
    StringBuilder ret;
    if (method.isStaticMethod()) {
      ret = new StringBuilder(Helper.getNonGenericType(model.getIfaceFQCN()));
    } else {
      ret = new StringBuilder("delegate");
    }
    ret.append(".").append(method.getName()).append("(");
    int index = 0;
    for (ParamInfo param : method.getParams()) {
      if (index > 0) {
        ret.append(", ");
      }
      TypeInfo type = param.getType();
      if (type.isParameterized() && (type.getRaw().getName().equals(Flux.class.getName()))) {
        String adapterFunction;
        ParameterizedTypeInfo parameterizedType = (ParameterizedTypeInfo) type;
        if (parameterizedType.getArg(0).isVariable()) {
          adapterFunction = "java.util.function.Function.identity()";
        } else {
          adapterFunction = "obj -> (" + parameterizedType.getArg(0).getRaw().getName() + ")obj.getDelegate()";
        }
        ret.append("io.vertx.reactor.impl.ReadStreamSubscriber.asReadStream(").append(param.getName()).append(",").append(adapterFunction).append(").resume()");
      } else {
        ret.append(genConvParam(type, method, param.getName()));
      }
      index = index + 1;
    }
    ret.append(")");
    return ret.toString();
  }

  private String genConvReturn(TypeInfo type, MethodInfo method, String expr) {
    ClassKind kind = type.getKind();
    if (kind == OBJECT) {
      if (type.isVariable()) {
        String typeArg = genTypeArg((TypeVariableInfo) type, method);
        if (typeArg != null) {
          return "(" + type.getName() + ")" + typeArg + ".wrap(" + expr + ")";
        }
      }
      return "(" + type.getSimpleName() + ") " + expr;
    } else if (isSameType(type, method)) {
      return expr;
    } else if (kind == API) {
      StringBuilder tmp = new StringBuilder(type.getRaw().translateName(id));
      tmp.append(".newInstance(");
      tmp.append(expr);
      if (type.isParameterized()) {
        ParameterizedTypeInfo parameterizedTypeInfo = (ParameterizedTypeInfo) type;
        for (TypeInfo arg : parameterizedTypeInfo.getArgs()) {
          tmp.append(", ");
          ClassKind argKind = arg.getKind();
          if (argKind == API) {
            tmp.append(arg.translateName(id)).append(".__TYPE_ARG");
          } else {
            String typeArg = "io.vertx.lang.rx.TypeArg.unknown()";
            if (argKind == OBJECT && arg.isVariable()) {
              String resolved = genTypeArg((TypeVariableInfo) arg, method);
              if (resolved != null) {
                typeArg = resolved;
              }
            }
            tmp.append(typeArg);
          }
        }
      }
      tmp.append(")");
      return tmp.toString();
    } else if (type.isParameterized()) {
      ParameterizedTypeInfo parameterizedTypeInfo = (ParameterizedTypeInfo) type;
      if (kind == HANDLER) {
        TypeInfo abc = parameterizedTypeInfo.getArg(0);
        if (abc.getKind() == ASYNC_RESULT) {
          TypeInfo tutu = ((ParameterizedTypeInfo) abc).getArg(0);
          return "new Handler<AsyncResult<" + genTypeName(tutu) + ">>() {\n" +
            "      public void handle(AsyncResult<" + genTypeName(tutu) + "> ar) {\n" +
            "        if (ar.succeeded()) {\n" +
            "          " + expr + ".handle(io.vertx.core.Future.succeededFuture(" + genConvParam(tutu, method, "ar.result()") + "));\n" +
            "        } else {\n" +
            "          " + expr + ".handle(io.vertx.core.Future.failedFuture(ar.cause()));\n" +
            "        }\n" +
            "      }\n" +
            "    }";
        } else {
          return "new Handler<" + genTypeName(abc) + ">() {\n" +
            "      public void handle(" + genTypeName(abc) + " event) {\n" +
            "          " + expr + ".handle(" + genConvParam(abc, method, "event") + ");\n" +
            "      }\n" +
            "    }";
        }
      } else if (kind == LIST || kind == SET) {
        return expr + ".stream().map(elt -> " + genConvReturn(parameterizedTypeInfo.getArg(0), method, "elt") + ").collect(java.util.stream.Collectors.to" + type.getRaw().getSimpleName() + "())";
      }
    }
    return expr;
  }

  private MethodInfo genFutureMethod(MethodInfo method) {
    String futMethodName = genFutureMethodName(method);
    List<ParamInfo> futParams = new ArrayList<>();
    int count = 0;
    int size = method.getParams().size() - 1;
    while (count < size) {
      ParamInfo param = method.getParam(count);
      /* Transform ReadStream -> Flowable */
      futParams.add(param);
      count = count + 1;
    }
    ParamInfo futParam = method.getParam(size);
    TypeInfo futType = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) futParam.getType()).getArg(0)).getArg(0);
    TypeInfo futUnresolvedType = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) futParam.getUnresolvedType()).getArg(0)).getArg(0);
    TypeInfo futReturnType;
    if (futUnresolvedType.isNullable()) {
      futReturnType = new io.vertx.codegen.type.ParameterizedTypeInfo(io.vertx.codegen.type.TypeReflectionFactory.create(Mono.class).getRaw(), false, Collections.singletonList(futType));
    } else {
      futReturnType = new io.vertx.codegen.type.ParameterizedTypeInfo(io.vertx.codegen.type.TypeReflectionFactory.create(Mono.class).getRaw(), false, Collections.singletonList(futType));
    }
    return method.copy().setName(futMethodName).setReturnType(futReturnType).setParams(futParams);
  }

  private MethodInfo genOverloadedMethod(MethodInfo method, Class streamType) {
    List<ParamInfo> params = null;
    int count = 0;
    for (ParamInfo param : method.getParams()) {
      if (param.getType().isParameterized() && param.getType().getRaw().getName().equals("io.vertx.core.streams.ReadStream")) {
        if (params == null) {
          params = new ArrayList<>(method.getParams());
        }
        ParameterizedTypeInfo paramType = new io.vertx.codegen.type.ParameterizedTypeInfo(
          io.vertx.codegen.type.TypeReflectionFactory.create(streamType).getRaw(),
          false,
          java.util.Collections.singletonList(((ParameterizedTypeInfo) param.getType()).getArg(0))
        );
        params.set(count, new io.vertx.codegen.ParamInfo(
          param.getIndex(),
          param.getName(),
          param.getDescription(),
          paramType
        ));
      }
      count = count + 1;
    }
    if (params != null) {
      return method.copy().setParams(params);
    }
    return null;
  }

  private String genConvParam(TypeInfo type, MethodInfo method, String expr) {
    ClassKind kind = type.getKind();
    if (isSameType(type, method)) {
      return expr;
    } else if (kind == OBJECT) {
      if (type.isVariable()) {
        String typeArg = genTypeArg((TypeVariableInfo) type, method);
        if (typeArg != null) {
          return typeArg + ".<" + type.getName() + ">unwrap(" + expr + ")";
        }
      }
      return expr;
    } else if (kind == API) {
      return expr + ".getDelegate()";
    } else if (kind == CLASS_TYPE) {
      return "io.vertx.lang." + id + ".Helper.unwrap(" + expr + ")";
    } else if (type.isParameterized()) {
      ParameterizedTypeInfo parameterizedTypeInfo = (ParameterizedTypeInfo) type;
      if (kind == HANDLER) {
        TypeInfo eventType = parameterizedTypeInfo.getArg(0);
        ClassKind eventKind = eventType.getKind();
        if (eventKind == ASYNC_RESULT) {
          TypeInfo resultType = ((ParameterizedTypeInfo) eventType).getArg(0);
          return "new Handler<AsyncResult<" + resultType.getName() + ">>() {\n" +
            "      public void handle(AsyncResult<" + resultType.getName() + "> ar) {\n" +
            "        if (ar.succeeded()) {\n" +
            "          " + expr + ".handle(io.vertx.core.Future.succeededFuture(" + genConvReturn(resultType, method, "ar.result()") + "));\n" +
            "        } else {\n" +
            "          " + expr + ".handle(io.vertx.core.Future.failedFuture(ar.cause()));\n" +
            "        }\n" +
            "      }\n" +
            "    }";
        } else {
          return "new Handler<" + eventType.getName() + ">() {\n" +
            "      public void handle(" + eventType.getName() + " event) {\n" +
            "        " + expr + ".handle(" + genConvReturn(eventType, method, "event") + ");\n" +
            "      }\n" +
            "    }";
        }
      } else if (kind == FUNCTION) {
        TypeInfo argType = parameterizedTypeInfo.getArg(0);
        TypeInfo retType = parameterizedTypeInfo.getArg(1);
        return "new java.util.function.Function<" + argType.getName() + "," + retType.getName() + ">() {\n" +
          "      public " + retType.getName() + " apply(" + argType.getName() + " arg) {\n" +
          "        " + genTypeName(retType) + " ret = " + expr + ".apply(" + genConvReturn(argType, method, "arg") + ");\n" +
          "        return " + genConvParam(retType, method, "ret") + ";\n" +
          "      }\n" +
          "    }";
      } else if (kind == LIST || kind == SET) {
        return expr + ".stream().map(elt -> " + genConvParam(parameterizedTypeInfo.getArg(0), method, "elt") + ").collect(java.util.stream.Collectors.to" + type.getRaw().getSimpleName() + "())";
      } else if (kind == MAP) {
        return expr + ".entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey(), e -> " + genConvParam(parameterizedTypeInfo.getArg(1), method, "e.getValue()") + "))";
      }
    }
    return expr;
  }

  private String genTypeArg(TypeVariableInfo typeVar, MethodInfo method) {
    if (typeVar.isClassParam()) {
      return "__typeArg_" + typeVar.getParam().getIndex();
    } else {
      TypeArgExpression typeArg = method.resolveTypeArg(typeVar);
      if (typeArg != null) {
        if (typeArg.isClassType()) {
          return "io.vertx.lang.rx.TypeArg.of(" + typeArg.getParam().getName() + ")";
        } else {
          return typeArg.getParam().getName() + ".__typeArg_" + typeArg.getIndex();
        }
      }
    }
    return null;
  }

  private boolean isSameType(TypeInfo type, MethodInfo method) {
    ClassKind kind = type.getKind();
    if (kind.basic || kind.json || kind == DATA_OBJECT || kind == ENUM || kind == OTHER || kind == THROWABLE || kind == VOID) {
      return true;
    } else if (kind == OBJECT) {
      if (type.isVariable()) {
        return !isReified((TypeVariableInfo) type, method);
      } else {
        return true;
      }
    } else if (type.isParameterized()) {
      ParameterizedTypeInfo parameterizedTypeInfo = (ParameterizedTypeInfo) type;
      if (kind == LIST || kind == SET || kind == ASYNC_RESULT) {
        return isSameType(parameterizedTypeInfo.getArg(0), method);
      } else if (kind == MAP) {
        return isSameType(parameterizedTypeInfo.getArg(1), method);
      } else if (kind == HANDLER) {
        return isSameType(parameterizedTypeInfo.getArg(0), method);
      } else if (kind == FUNCTION) {
        return isSameType(parameterizedTypeInfo.getArg(0), method) && isSameType(parameterizedTypeInfo.getArg(1), method);
      }
    }
    return false;
  }

  private boolean isReified(TypeVariableInfo typeVar, MethodInfo method) {
    if (typeVar.isClassParam()) {
      return true;
    } else {
      TypeArgExpression typeArg = method.resolveTypeArg(typeVar);
      return typeArg != null && typeArg.isClassType();
    }
  }
}

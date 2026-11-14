package com.ylf.cms;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模块边界校验测试（M2 提交前必跑）
 *
 * 校验规则：
 * 1. 归属校验：每个 java 文件必须归属到指定模块包（com.ylf.cms.{common|config|security|content|catalog|ops|startup}），
 *    不允许出现无归属文件。
 * 2. 越界校验：业务模块禁止 import 其他业务模块的 entity/mapper/controller/config 包，
 *    只允许调用对方的 service / dto / vo 数据契约，以及公共层 common / config。
 *
 * 运行方式：
 *   mvn -o test -Dtest=ModuleBoundaryTest
 * 或直接在 IDEA 中右键运行本测试类。
 */
class ModuleBoundaryTest {

    /** 业务模块（互不允许访问对方内部包） */
    private static final Set<String> BUSINESS_MODULES = Set.of("security", "content", "catalog", "ops");

    /** 模块内部包：禁止跨模块访问 */
    private static final Set<String> INTERNAL_SUBPACKAGES = Set.of("entity", "mapper", "controller", "config");

    /** 公共模块：所有模块可引用 */
    private static final Set<String> PUBLIC_MODULES = Set.of("common", "config");

    @Test
    void testModuleBoundary() throws IOException {
        // 测试在 startup 模块运行，cms 根 = 上一级目录
        Path root = Paths.get("..").toAbsolutePath().normalize();
        List<String> errors = new ArrayList<>();

        // 归属校验：所有模块目录下的 java 文件 package 前缀必须匹配模块名
        for (String module : BUSINESS_MODULES) {
            checkBelonging(root, module, errors);
        }
        checkBelonging(root, "common", errors);
        checkBelonging(root, "config", errors);

        // 越界校验：业务模块之间的非法 import
        for (String module : BUSINESS_MODULES) {
            checkIllegalImports(root, module, errors);
        }

        assertTrue(errors.isEmpty(),
                "模块边界校验失败，共 " + errors.size() + " 处违规：\n" + String.join("\n", errors));
    }

    /** 归属校验：文件 package 必须以 com.ylf.cms.<module> 开头 */
    private void checkBelonging(Path root, String module, List<String> errors) throws IOException {
        Path src = root.resolve(module).resolve("src/main/java");
        if (!Files.exists(src)) {
            return;
        }
        String expectPrefix = "com.ylf.cms." + module;
        try (Stream<Path> files = Files.walk(src)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().endsWith("package-info.java"))
                    .forEach(f -> {
                        String pkg = readPackage(f);
                        if (pkg == null || !pkg.startsWith(expectPrefix)) {
                            errors.add("【归属违规】" + f + " 的包名必须以 " + expectPrefix + " 开头，实际：" + pkg);
                        }
                    });
        }
    }

    /** 越界校验：禁止 import 其他业务模块的 entity/mapper/controller/config */
    private void checkIllegalImports(Path root, String module, List<String> errors) throws IOException {
        Path src = root.resolve(module).resolve("src/main/java");
        if (!Files.exists(src)) {
            return;
        }
        try (Stream<Path> files = Files.walk(src)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .forEach(f -> {
                        List<String> imports = readImports(f);
                        for (String imp : imports) {
                            if (isIllegalImport(imp, module)) {
                                errors.add("【越界违规】" + relative(root, f) + " 非法 import: " + imp
                                        + "（禁止跨模块引用对方 entity/mapper/controller/config，请改调对方 Service）");
                            }
                        }
                    });
        }
    }

    /** 判断 import 是否越界 */
    private boolean isIllegalImport(String imp, String currentModule) {
        for (String other : BUSINESS_MODULES) {
            if (other.equals(currentModule)) {
                continue;
            }
            String prefix = "com.ylf.cms." + other + ".";
            if (imp.startsWith(prefix)) {
                String rest = imp.substring(prefix.length());
                int dot = rest.indexOf('.');
                String firstSub = dot < 0 ? rest : rest.substring(0, dot);
                if (INTERNAL_SUBPACKAGES.contains(firstSub)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String readPackage(Path file) {
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.startsWith("package ") && t.endsWith(";")) {
                    return t.substring(8, t.length() - 1).trim();
                }
                if (t.startsWith("import ") || t.startsWith("class ") || t.startsWith("interface ") || t.startsWith("public ")) {
                    break;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private List<String> readImports(Path file) {
        List<String> imports = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.startsWith("import ") && t.endsWith(";") && !t.startsWith("import static")) {
                    imports.add(t.substring(7, t.length() - 1).trim());
                }
            }
        } catch (IOException ignored) {
        }
        return imports;
    }

    private String relative(Path root, Path file) {
        return root.relativize(file).toString().replace('\\', '/');
    }
}

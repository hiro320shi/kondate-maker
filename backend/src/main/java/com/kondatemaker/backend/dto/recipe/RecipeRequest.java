package com.kondatemaker.backend.dto.recipe;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecipeRequest {

    @NotBlank(message = "料理名を入力してください")
    @Size(max = 50, message = "料理名は50文字以内で入力してください")
    private String name;

    @NotNull(message = "カテゴリを選択してください")
    private Long categoryId;

    @NotNull(message = "ポイントを入力してください")
    @DecimalMin(value = "0.1", message = "ポイントは0.1以上で入力してください")
    @DecimalMax(value = "1.0", message = "ポイントは1.0以下で入力してください")
    private BigDecimal point;

    @Size(max = 500, message = "メモは500文字以内で入力してください")
    private String memo;
}
